package com.jonabai.projects.apnea.services.impl;

import com.jonabai.projects.apnea.api.domain.BreathingPause;
import com.jonabai.projects.apnea.api.domain.SilenceDetectionException;
import com.jonabai.projects.apnea.services.BreathingPauseOutputWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Writes the output to a CSV file using Apache Commons CSV.
 */
@Service
public class BreathingPauseOutputWriterImpl implements BreathingPauseOutputWriter {

    private static final String[] HEADERS = {
            "File Path", "Pause #", "start [secs]", "end [secs]", "duration [secs]", "type"
    };

    @Override
    public void writeOutput(String outputPath, List<BreathingPause> pauseList) {
        if (pauseList == null || outputPath == null || outputPath.isBlank()) {
            return;
        }

        var csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(HEADERS)
                .build();

        try (Writer writer = Files.newBufferedWriter(Path.of(outputPath));
             CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {

            for (var pause : pauseList) {
                csvPrinter.printRecord(
                        pause.filePath(),
                        pause.index(),
                        pause.start(),
                        pause.end(),
                        pause.duration(),
                        pause.type().name()
                );
            }

        } catch (IOException e) {
            throw new SilenceDetectionException("Error exporting results to " + outputPath, e);
        }
    }
}
