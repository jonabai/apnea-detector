package com.jonabai.projects.apnea.services.impl;

import com.csvreader.CsvWriter;
import com.jonabai.projects.apnea.api.domain.BreathingPause;
import com.jonabai.projects.apnea.api.domain.SilenceDetectionException;
import com.jonabai.projects.apnea.services.BreathingPauseOutputWriter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static com.csvreader.CsvWriter.ESCAPE_MODE_BACKSLASH;

/**
 * Writes the output to a csv file
 */
@Service
public class BreathingPauseOutputWriterImpl implements BreathingPauseOutputWriter {

    private static final char RECORD_DELIMITER = '\n';

    @Override
    public void writeOutput(String outputPath, List<BreathingPause> pauseList) {
        if(pauseList == null || StringUtils.isEmpty(outputPath))
            return;

        CsvWriter csvOutput = null;
        try(FileWriter fileWriter = new FileWriter(outputPath, false)) {
            csvOutput = new CsvWriter(fileWriter, ',');
            csvOutput.setUseTextQualifier(false);
            csvOutput.setEscapeMode(ESCAPE_MODE_BACKSLASH);
            csvOutput.setRecordDelimiter(RECORD_DELIMITER);

            writeHeader(csvOutput);
            for(BreathingPause pause : pauseList)
                writePause(csvOutput, pause);
        } catch (IOException e) {
            throw new SilenceDetectionException("Error exporting results", e);
        } finally {
            if(csvOutput != null)
                csvOutput.close();
        }
    }

    private void writeHeader(CsvWriter csvOutput) throws IOException {
        csvOutput.write("File Path");
        csvOutput.write("Pause #");
        csvOutput.write("start [secs]");
        csvOutput.write("end [secs]");
        csvOutput.write("duration [secs]");
        csvOutput.write("type");
        csvOutput.endRecord();
    }
    private void writePause(CsvWriter csvOutput, BreathingPause pause) throws IOException {
        csvOutput.write(pause.getFilePath());
        csvOutput.write(String.valueOf(pause.getIndex()));
        csvOutput.write(String.valueOf(pause.getStart()));
        csvOutput.write(String.valueOf(pause.getEnd()));
        csvOutput.write(String.valueOf(pause.getEnd() - pause.getStart()));
        csvOutput.write(pause.getType().name());
        csvOutput.endRecord();
    }
}
