package com.example.demo.service;

import com.example.demo.entity.InspectionData;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {
    
    @Autowired
    private InspectDataService inspectDataService;
    
    @Value("${report.storage.path}")
    private String reportStoragePath;
    
    // 生成PDF报告并返回字节数组
    public byte[] generatePdfReport(List<InspectionData> dataList, String reportType, String criteria) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate()); // 横向A4纸
        
        // 创建PDF写入器
        PdfWriter.getInstance(document, outputStream);
        document.open();
        
        // 添加报告标题
        addReportTitle(document, reportType, criteria);
        
        // 添加报告表格
        addDataTable(document, dataList);
        
        document.close();
        return outputStream.toByteArray();
    }
    
    // 添加报告标题
    private void addReportTitle(Document document, String reportType, String criteria) throws DocumentException {
        // 创建标题段落
        Paragraph title = new Paragraph("药品检测报告", 
            new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD));//字体-字号-加粗
        title.setAlignment(Element.ALIGN_CENTER);//居中
        title.setSpacingAfter(20f);//标题和正文间距
        document.add(title);
        
        // 创建报告类型和条件段落
        Paragraph subTitle = new Paragraph();
        subTitle.add(new Chunk("报告类型: ", 
            new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
        subTitle.add(new Chunk(reportType, 
            new Font(Font.FontFamily.HELVETICA, 12)));
        subTitle.add(Chunk.NEWLINE);
        subTitle.add(new Chunk("筛选条件: ", 
            new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
        subTitle.add(new Chunk(criteria, 
            new Font(Font.FontFamily.HELVETICA, 12)));
        subTitle.setSpacingAfter(20f);
        document.add(subTitle);
    }
    
    // 添加数据表格
    private void addDataTable(Document document, List<InspectionData> dataList) throws DocumentException {
        // 创建表格，12列
        PdfPTable table = new PdfPTable(12);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
        
        // 设置表格列宽
        float[] columnWidths = {1f, 1.5f, 1.5f, 1.5f, 1f, 1f, 1f, 1f, 1f, 1f, 1.5f, 1.5f};
        table.setWidths(columnWidths);
        
        // 添加表头
        addTableHeader(table);
        
        // 添加数据行
        addDataRows(table, dataList);
        
        document.add(table);
    }
    
    // 添加表头
    private void addTableHeader(PdfPTable table) {
        // 表头单元格样式
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(new BaseColor(70, 130, 180)); // 钢蓝色背景
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setPadding(5);
        
        // 添加表头列
        headerCell.setPhrase(new Phrase("产品编号", headerFont));
        table.addCell(headerCell);
        
        headerCell.setPhrase(new Phrase("产品名称", headerFont));
        table.addCell(headerCell);
        
        headerCell.setPhrase(new Phrase("批次号", headerFont));
        table.addCell(headerCell);
        
        headerCell.setPhrase(new Phrase("检测时间", headerFont));
        table.addCell(headerCell);
        
        headerCell.setPhrase(new Phrase("设备编号", headerFont));
        table.addCell(headerCell);
        
        headerCell.setPhrase(new Phrase("重量(mg)", headerFont));
        table.addCell(headerCell);
        
        headerCell.setPhrase(new Phrase("厚度(mm)", headerFont));
        table.addCell(headerCell);
        
        headerCell.setPhrase(new Phrase("硬度(N)", headerFont));
        table.addCell(headerCell);
        
        headerCell.setPhrase(new Phrase("直径(mm)", headerFont));
        table.addCell(headerCell);
        
        headerCell.setPhrase(new Phrase("脆碎度(%)", headerFont));
        table.addCell(headerCell);
        
        headerCell.setPhrase(new Phrase("设备名称", headerFont));
        table.addCell(headerCell);
        
        headerCell.setPhrase(new Phrase("设备位置", headerFont));
        table.addCell(headerCell);
    }
    
    // 添加数据行
    private void addDataRows(PdfPTable table, List<InspectionData> dataList) {
        // 数据单元格样式
        Font dataFont = new Font(Font.FontFamily.HELVETICA, 9);
        PdfPCell cell = new PdfPCell();
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        
        // 日期时间格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (InspectionData data : dataList) {
            // 产品编号
            cell.setPhrase(new Phrase(data.getProduct().getProductId(), dataFont));
            table.addCell(cell);
            
            // 产品名称
            cell.setPhrase(new Phrase(data.getProduct().getProductName(), dataFont));
            table.addCell(cell);
            
            // 批次号
            cell.setPhrase(new Phrase(data.getBatchNo(), dataFont));
            table.addCell(cell);
            
            // 检测时间
            cell.setPhrase(new Phrase(data.getInspectTime().format(formatter), dataFont));
            table.addCell(cell);
            
            // 设备编号
            cell.setPhrase(new Phrase(data.getDevice().getDeviceCode(), dataFont));
            table.addCell(cell);
            
            // 重量
            cell.setPhrase(new Phrase(data.getWeight() != null ? data.getWeight().toString() : "N/A", dataFont));
            table.addCell(cell);
            
            // 厚度
            cell.setPhrase(new Phrase(data.getThickness() != null ? data.getThickness().toString() : "N/A", dataFont));
            table.addCell(cell);
            
            // 硬度
            cell.setPhrase(new Phrase(data.getHardness() != null ? data.getHardness().toString() : "N/A", dataFont));
            table.addCell(cell);
            
            // 直径
            cell.setPhrase(new Phrase(data.getDiameter() != null ? data.getDiameter().toString() : "N/A", dataFont));
            table.addCell(cell);
            
            // 脆碎度
            cell.setPhrase(new Phrase(data.getFriability() != null ? data.getFriability().toString() : "N/A", dataFont));
            table.addCell(cell);
            
            // 设备名称
            cell.setPhrase(new Phrase(data.getDevice().getDeviceName(), dataFont));
            table.addCell(cell);
            
            // 设备位置
            cell.setPhrase(new Phrase(data.getDevice().getLocation(), dataFont));
            table.addCell(cell);
        }
    }
    
    // 保存PDF文件到服务器
    public String savePdfReport(byte[] pdfBytes, String fileName) throws Exception {
        // 确保存储目录存在
        java.nio.file.Path path = java.nio.file.Paths.get(reportStoragePath);
        if (!java.nio.file.Files.exists(path)) {
            java.nio.file.Files.createDirectories(path);
        }
        
        // 构建完整文件路径
        String fullPath = reportStoragePath + "/" + fileName;
        
        // 写入文件
        try (FileOutputStream fos = new FileOutputStream(fullPath)) {
            fos.write(pdfBytes);
        }
        
        return fullPath;
    }
}