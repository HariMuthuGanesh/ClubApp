package com.clubapp.service;

import com.clubapp.entity.Event;
import com.clubapp.entity.User;
import com.clubapp.exception.ResourceNotFoundException;
import com.clubapp.repository.AttendanceRepository;
import com.clubapp.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AttendanceExportService {

    private final EventRepository eventRepository;
    private final AttendanceRepository attendanceRepository;

    public byte[] generateAttendanceExcel(Long eventId, User currentUser) throws IOException {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));
        
        if (currentUser.getRole() == com.clubapp.entity.Role.COORDINATOR
                && (event.getClub().getCoordinator() == null || !event.getClub().getCoordinator().getId().equals(currentUser.getId()))) {
            throw new IllegalArgumentException("Access denied.");
        }

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Attendance");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Name", "Email", "Department", "Year", "Status", "Utterance"};
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerStyle);
            }

            // Populate data
            int rowIdx = 1;
            java.util.List<com.clubapp.entity.Attendance> attendanceRecords = attendanceRepository.findByEvent(event);
            java.util.Map<Long, String> statusMap = attendanceRecords.stream()
                    .collect(java.util.stream.Collectors.toMap(a -> a.getUser().getId(), a -> a.getStatus().name()));

            for (User u : event.getAttendees()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(u.getName() != null ? u.getName() : "");
                row.createCell(1).setCellValue(u.getEmail() != null ? u.getEmail() : "");
                row.createCell(2).setCellValue(u.getDepartment() != null ? u.getDepartment() : "");
                row.createCell(3).setCellValue(u.getYear() != null ? u.getYear() : "");
                row.createCell(4).setCellValue(statusMap.getOrDefault(u.getId(), "REGISTERED"));
                
                // Fetch the specific utterance for this user
                String utterance = attendanceRecords.stream()
                    .filter(a -> a.getUser().getId().equals(u.getId()))
                    .map(com.clubapp.entity.Attendance::getUtterance)
                    .findFirst().orElse("");
                row.createCell(5).setCellValue(utterance);
            }

            // Auto-size columns
            for (int col = 0; col < headers.length; col++) {
                sheet.autoSizeColumn(col);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
