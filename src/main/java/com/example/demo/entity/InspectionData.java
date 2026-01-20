package com.example.demo.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
@Table(name = "inspection_data")
public class InspectionData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "data_id", nullable = false)
    private Long dataId;

    @ManyToOne
    @JoinColumn(name = "device_id", referencedColumnName = "deviceid")
    private Device device;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "product_id", nullable = false)
    private Product product;

    @Column(name = "batch_no", length = 30, nullable = false)
    private String batchNo;

    @Column(name = "inspect_time", nullable = false)
    private ZonedDateTime inspectTime;

    @Column(name = "weight", precision = 10, scale = 2)
    private BigDecimal weight; // 单位: mg

    @Column(name = "thickness", precision = 10, scale = 2)
    private BigDecimal thickness; // 单位: mm

    @Column(name = "hardness", precision = 10, scale = 2)
    private BigDecimal hardness; // 单位: N

    @Column(name = "diameter", precision = 10, scale = 2)
    private BigDecimal diameter; // 单位: mm

    @Column(name = "friability", precision = 10, scale = 2)
    private BigDecimal friability; // 单位: %

    @Column(name = "is_valid", nullable = false, columnDefinition = "TINYINT")
    private Integer isValid ;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    public InspectionData() {
    }

    // Getter和Setter方法
    public Long getDataId() {
        return dataId;
    }

    public void setDataId(Long dataId) {
        this.dataId = dataId;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

   public Product getProduct() {
        return product;
    }
  

    public void setProduct(Product product) {
        this.product = product;
    }
    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public ZonedDateTime getInspectTime() {
        return inspectTime;
    }

    public void setInspectTime(ZonedDateTime inspectTime) {
        this.inspectTime = inspectTime;
    }



    public Integer getIsValid() {
        return isValid;
    }

    public void setIsValid(Integer valid) {
        isValid = valid;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public BigDecimal getThickness() {
        return thickness;
    }

    public void setThickness(BigDecimal thickness) {
        this.thickness = thickness;
    }

    public BigDecimal getHardness() {
        return hardness;
    }

    public void setHardness(BigDecimal hardness) {
        this.hardness = hardness;
    }

    public BigDecimal getDiameter() {
        return diameter;
    }

    public void setDiameter(BigDecimal diameter) {
        this.diameter = diameter;
    }

    public BigDecimal getFriability() {
        return friability;
    }

    public void setFriability(BigDecimal friability) {
        this.friability = friability;
    }
    // 时间自动处理
    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }

    // // 可以添加的枚举（根据需求）
    // public enum InspectionStatus {
    //     PASSED, FAILED, PENDING_REVIEW
    // }
}