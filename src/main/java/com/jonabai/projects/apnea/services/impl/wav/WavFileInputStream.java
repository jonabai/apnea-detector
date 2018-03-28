package com.jonabai.projects.apnea.services.impl.wav;

import com.jonabai.projects.apnea.api.domain.WavFileException;
import com.jonabai.projects.apnea.api.domain.WavFileIOState;
import com.jonabai.projects.apnea.services.WavFile;

import java.io.*;

/**
 * WavFile implementation based in a FileInputStream
 */
public class WavFileInputStream implements WavFile, AutoCloseable {
    private static final int BUFFER_SIZE = 4096;

    private static final int FMT_CHUNK_ID = 0x20746D66;
    private static final int DATA_CHUNK_ID = 0x61746164;
    private static final int RIFF_CHUNK_ID = 0x46464952;
    private static final int RIFF_TYPE_ID = 0x45564157;
    private static final int HEADER_INFO_SIZE = 16;

    private File file;						// File that will be read from or written to
    private WavFileIOState ioState;				// Specifies the IO State of the Wav File (used for sanity checking)
    private int bytesPerSample;			// Number of bytes required to store a single sample
    private long numFrames;					// Number of frames within the data section
    private FileOutputStream oStream;	// Output stream used for writing data
    private FileInputStream iStream;		// Input stream used for reading data
    private double floatScale;				// Scaling factor used for int <-> float conversion
    private double floatOffset;			// Offset factor used for int <-> float conversion

    // Wav Header
    private int numChannels;				// 2 bytes unsigned, 0x0001 (1) to 0xFFFF (65,535)
    private long sampleRate;				// 4 bytes unsigned, 0x00000001 (1) to 0xFFFFFFFF (4,294,967,295)
    // Although a java int is 4 bytes, it is signed, so need to use a long
    private int blockAlign;					// 2 bytes unsigned, 0x0001 (1) to 0xFFFF (65,535)
    private int validBits;					// 2 bytes unsigned, 0x0002 (2) to 0xFFFF (65,535)

    // Buffering
    private byte[] buffer;					// Local buffer used for IO
    private int bufferPointer;				// Points to the current position in local buffer
    private int bytesRead;					// Bytes read after last read into local buffer
    private long frameCounter;				// Current number of frames read or written

    // Cannot instantiate WavFile directly, must either use newWavFile() or openWavFile()
    public WavFileInputStream(File file) throws WavFileException {
        buffer = new byte[BUFFER_SIZE];
        this.file = file;

        // Create a new file input stream for reading file data
        try {
            this.iStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new WavFileException("Error opening teh input stream!", e);
        }

        // Read the first 12 bytes of the file
        readWavFileHeader(file);

        // Search for the Format and Data Chunks
        try {
            readWavFileMetadata();
        } catch (IOException e) {
            throw new WavFileException("Error reading the Format and Data Chunks!", e);
        }
    }

    @Override
    public int getNumChannels()
    {
        return numChannels;
    }

    @Override
    public long getNumFrames()
    {
        return numFrames;
    }

    @Override
    public long getSampleRate()
    {
        return sampleRate;
    }

    @Override
    public int getValidBits()
    {
        return validBits;
    }

    @Override
    public int getBytesPerSample() {
        return bytesPerSample;
    }

    @Override
    public int getBlockAlign() {
        return blockAlign;
    }

    @Override
    public File getFile() {
        return file;
    }

    private void readWavFileMetadata() throws IOException, WavFileException {
        long chunkSize;
        boolean foundFormat = false;
        while (true)
        {
            // Read the first 8 bytes of the chunk (ID and chunk size)
            int bytesReadMetadata = this.iStream.read(this.buffer, 0, 8);
            if (bytesReadMetadata == -1)
                throw new WavFileException("Reached end of file without finding format chunk");
            if (bytesReadMetadata != 8)
                throw new WavFileException("Could not read chunk header");

            // Extract the chunk ID and Size
            long chunkID = getLE(this.buffer, 0, 4);
            chunkSize = getLE(this.buffer, 4, 4);

            // Word align the chunk size
            // chunkSize specifies the number of bytes holding data. However,
            // the data should be word aligned (2 bytes) so we need to calculate
            // the actual number of bytes in the chunk
            long numChunkBytes = (chunkSize%2 == 1) ? chunkSize+1 : chunkSize;

            if (chunkID == FMT_CHUNK_ID)
            {
                // Flag that the format chunk has been found
                foundFormat = true;
                extractFormatChunkValues(numChunkBytes);
            }
            else if (chunkID == DATA_CHUNK_ID)
            {
                extractDataChunkID(chunkSize, foundFormat);
                break;
            }
            else
            {
                // If an unknown chunk ID is found, just skip over the chunk data
                if(this.iStream.skip(numChunkBytes) != numChunkBytes) {
                    throw new WavFileException("Extract data chunk: Not enough bytes in the file!");
                }
            }
        }

        // Calculate the scaling factor for converting to a normalised double
        calculateScalingFactor();

        this.bufferPointer = 0;
        this.bytesRead = 0;
        this.frameCounter = 0;
        this.ioState = WavFileIOState.READING;
    }

    private void calculateScalingFactor() {
        if (this.validBits > 8)
        {
            // If more than 8 validBits, data is signed
            // Conversion required dividing by magnitude of max negative value
            this.floatOffset = 0;
            this.floatScale = 1 << (this.validBits - 1);
        }
        else
        {
            // Else if 8 or less validBits, data is unsigned
            // Conversion required dividing by max positive value
            this.floatOffset = -1;
            this.floatScale = 0.5 * ((1 << this.validBits) - 1);
        }
    }

    private void extractDataChunkID(long chunkSize, boolean foundFormat) throws WavFileException {
        // Check if we've found the format chunk,
        // If not, throw an exception as we need the format information
        // before we can read the data chunk
        if (!foundFormat)
            throw new WavFileException("Data chunk found before Format chunk");

        // Check that the chunkSize (wav data length) is a multiple of the
        // block align (bytes per frame)
        if (chunkSize % this.blockAlign != 0)
            throw new WavFileException("Data Chunk size is not multiple of Block Align");

        // Calculate the number of frames
        this.numFrames = chunkSize / this.blockAlign;
    }

    private void readWavFileHeader(File file) throws WavFileException {
        int bytesReadHeader;
        try {
            bytesReadHeader = this.iStream.read(this.buffer, 0, 12);
        } catch (IOException e) {
            throw new WavFileException("Error reading the header of the file!", e);
        }
        if (bytesReadHeader != 12)
            throw new WavFileException("Not enough wav file bytes for header");

        // Extract parts from the header
        long riffChunkID = getLE(this.buffer, 0, 4);
        long chunkSize = getLE(this.buffer, 4, 4);
        long riffTypeID = getLE(this.buffer, 8, 4);

        // Check the header bytes contains the correct signature
        if (riffChunkID != RIFF_CHUNK_ID)
            throw new WavFileException("Invalid Wav Header data, incorrect riff chunk ID");
        if (riffTypeID != RIFF_TYPE_ID)
            throw new WavFileException("Invalid Wav Header data, incorrect riff type ID");

        // Check that the file size matches the number of bytes listed in header
        if (file.length() != chunkSize+8) {
            throw new WavFileException("Header chunk size (" + chunkSize + ") does not match file size (" + file.length() + ")");
        }
    }

    private void extractFormatChunkValues(long numChunkBytes) throws IOException, WavFileException {
        // Read in the header info
        if(this.iStream.read(this.buffer, 0, HEADER_INFO_SIZE) != HEADER_INFO_SIZE)
            throw new WavFileException("Header is not long enough!");

        // Check this is uncompressed data
        int compressionCode = (int) getLE(this.buffer, 0, 2);
        if (compressionCode != 1) throw new WavFileException("Compression Code " + compressionCode + " not supported");

        // Extract the format information
        this.numChannels = (int) getLE(this.buffer, 2, 2);
        this.sampleRate = getLE(this.buffer, 4, 4);
        this.blockAlign = (int) getLE(this.buffer, 12, 2);
        this.validBits = (int) getLE(this.buffer, 14, 2);

        if (this.numChannels == 0)
            throw new WavFileException("Number of channels specified in header is equal to zero");
        if (this.blockAlign == 0)
            throw new WavFileException("Block Align specified in header is equal to zero");
        if (this.validBits < 2)
            throw new WavFileException("Valid Bits specified in header is less than 2");
        if (this.validBits > 64)
            throw new WavFileException("Valid Bits specified in header is greater than 64, this is greater than a long can hold");

        // Calculate the number of bytes required to hold 1 sample
        this.bytesPerSample = (this.validBits + 7) / 8;
        if (this.bytesPerSample * this.numChannels != this.blockAlign)
            throw new WavFileException("Block Align does not agree with bytes required for validBits and number of channels");

        // Account for number of format bytes and then skip over
        // any extra format bytes
        numChunkBytes -= 16;
        if(numChunkBytes > 0 && this.iStream.skip(numChunkBytes) != numChunkBytes) {
            throw new WavFileException("Extract format: Not enough bytes in the file!");
        }
    }

    // Get and Put little endian data from local buffer
    // ------------------------------------------------
    private static long getLE(byte[] buffer, int pos, int numBytes)
    {
        numBytes --;
        pos += numBytes;

        long val = buffer[pos] & 0xFF;
        for (int b=0 ; b<numBytes ; b++) val = (val << 8) + (buffer[--pos] & 0xFF);

        return val;
    }

    private long readSample() throws IOException, WavFileException
    {
        long val = 0;

        for (int b=0 ; b<bytesPerSample ; b++)
        {
            if (bufferPointer == bytesRead)
            {
                int read = iStream.read(buffer, 0, BUFFER_SIZE);
                if (read == -1) throw new WavFileException("Not enough data available");
                bytesRead = read;
                bufferPointer = 0;
            }

            int v = buffer[bufferPointer];
            if (b < bytesPerSample-1 || bytesPerSample == 1) v &= 0xFF;
            val += v << (b * 8);

            bufferPointer ++;
        }

        return val;
    }

    // Double
    // ------
    @Override
    public int readFrames(double[] sampleBuffer, int numFramesToRead) throws WavFileException
    {
        try {
            return readFrames(sampleBuffer, 0, numFramesToRead);
        } catch (IOException e) {
            throw new WavFileException("Error reading frames!", e);
        }
    }

    @Override
    public int readFrames(double[] sampleBuffer, int offset, int numFramesToRead) throws IOException, WavFileException
    {
        if (ioState != WavFileIOState.READING) throw new IOException("Cannot read from WavFile instance");

        for (int f=0 ; f<numFramesToRead ; f++)
        {
            if (frameCounter == numFrames) return f;

            for (int c=0 ; c<numChannels ; c++)
            {
                sampleBuffer[offset] = floatOffset + (double) readSample() / floatScale;
                offset ++;
            }

            frameCounter ++;
        }

        return numFramesToRead;
    }

    @Override
    public void close() throws IOException
    {
        // Close the input stream and set to null
        if (iStream != null)
        {
            iStream.close();
            iStream = null;
        }

        if (oStream != null)
        {
            // Write out anything still in the local buffer
            if (bufferPointer > 0) oStream.write(buffer, 0, bufferPointer);

            // Close the stream and set to null
            oStream.close();
            oStream = null;
        }

        // Flag that the stream is closed
        ioState = WavFileIOState.CLOSED;
    }


}

