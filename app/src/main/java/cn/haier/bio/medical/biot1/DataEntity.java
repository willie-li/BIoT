package cn.haier.bio.medical.biot1;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class DataEntity {
    private int cavityTemperature;
    private int sampleTemperature;
    private int settingTemperature;
    private int environmentTemperature;



    /***  传感器状态
     * Bit0:     样本传感器故障；
     * Bit1:     腔体传感器故障；
     * Bit2:     外环温传感器故障；
     */
    private byte sensorStatus;

    /***  报警状态
     * Bit0:     高温报警故障；
     * Bit1:     低温报警故障；
     * Bit2:     升温速率异常报警；
     * Bit3:     降温速率异常报警；
     * Bit4:     断电报警
     * Bit5:     开门报警
     */
    private byte alarmStatus;

    /***  运行状态
     * Bit0:箱体开关状态,=0关机 =1 开机;
     * Bit1:程序执行状态,=0未执行 =1 执行;
     * Bit2; 箱体干燥程序状态,=1干燥完成
     */
    private byte runningStatus;

    /***  部件状态
     * Bit0:    电磁阀1的状态
     * Bit1:    电磁阀2的状态
     * Bit2:    风机状态
     * Bit3:    加热管状态
     * Bit4:    门开关状态
     * Bit5:    充放电状态
     * Bit6:    电磁锁状态
     *
     */
    private byte componentStatus;

    private byte batteryVoltage;            //电池电压

    private byte programModel;              //编程模式
    private byte programState;              //程序执行状态
    private byte programTaskIndex;          //当前程序执行步骤
    private byte programIndex;              //当前程序编号

    private byte takeoverState;             //接管状态

    private byte liquidResidue;             //液氮余量

    private byte highVersion;               //主板程序版本：高位
    private byte lowerVersion;              //主板程序版本：低位

    private int supplyVoltage;             //电源电压


    public DataEntity() {

    }

    public int getCavityTemperature() {
        return cavityTemperature;
    }

    public void setCavityTemperature(int cavityTemperature) {
        this.cavityTemperature = cavityTemperature;
    }

    public int getSampleTemperature() {
        return sampleTemperature;
    }

    public void setSampleTemperature(int sampleTemperature) {
        this.sampleTemperature = sampleTemperature;
    }

    public int getSettingTemperature() {
        return settingTemperature;
    }

    public void setSettingTemperature(int settingTemperature) {
        this.settingTemperature = settingTemperature;
    }

    public int getEnvironmentTemperature() {
        return environmentTemperature;
    }

    public void setEnvironmentTemperature(int environmentTemperature) {
        this.environmentTemperature = environmentTemperature;
    }

    public byte getSensorStatus() {
        return sensorStatus;
    }

    public void setSensorStatus(byte sensorStatus) {
        this.sensorStatus = sensorStatus;
    }

    public byte getAlarmStatus() {
        return alarmStatus;
    }

    public void setAlarmStatus(byte alarmStatus) {
        this.alarmStatus = alarmStatus;
    }

    public byte getRunningStatus() {
        return runningStatus;
    }

    public void setRunningStatus(byte runningStatus) {
        this.runningStatus = runningStatus;
    }

    public byte getComponentStatus() {
        return componentStatus;
    }

    public void setComponentStatus(byte componentStatus) {
        this.componentStatus = componentStatus;
    }

    public byte getBatteryVoltage() {
        return batteryVoltage;
    }

    public void setBatteryVoltage(byte batteryVoltage) {
        this.batteryVoltage = batteryVoltage;
    }

    public byte getProgramModel() {
        return programModel;
    }

    public void setProgramModel(byte programModel) {
        this.programModel = programModel;
    }

    public byte getProgramState() {
        return programState;
    }

    public void setProgramState(byte programState) {
        this.programState = programState;
    }

    public byte getProgramTaskIndex() {
        return programTaskIndex;
    }

    public void setProgramTaskIndex(byte programTaskIndex) {
        this.programTaskIndex = programTaskIndex;
    }

    public byte getProgramIndex() {
        return programIndex;
    }

    public void setProgramIndex(byte programIndex) {
        this.programIndex = programIndex;
    }

    public byte getTakeoverState() {
        return takeoverState;
    }

    public void setTakeoverState(byte takeoverState) {
        this.takeoverState = takeoverState;
    }

    public byte getLiquidResidue() {
        return liquidResidue;
    }

    public void setLiquidResidue(byte liquidResidue) {
        this.liquidResidue = liquidResidue;
    }

    public byte getHighVersion() {
        return highVersion;
    }

    public void setHighVersion(byte highVersion) {
        this.highVersion = highVersion;
    }

    public byte getLowerVersion() {
        return lowerVersion;
    }

    public void setLowerVersion(byte lowerVersion) {
        this.lowerVersion = lowerVersion;
    }

    public int getSupplyVoltage() {
        return supplyVoltage;
    }

    public void setSupplyVoltage(int supplyVoltage) {
        this.supplyVoltage = supplyVoltage;
    }

    public static DataEntity fromBuffer(byte[] data) {
        ByteBuf buffer = Unpooled.copiedBuffer(data);
        buffer.skipBytes(4); //跳过头、长度和命令字节
        DataEntity entity = new DataEntity();
        entity.setCavityTemperature(0xFFFF & buffer.readShort());
        entity.setSampleTemperature(0xFFFF & buffer.readShort());
        entity.setSettingTemperature(0xFFFF & buffer.readShort());
        entity.setEnvironmentTemperature(0xFFFF & buffer.readShort());

        entity.setSensorStatus(buffer.readByte());
        entity.setAlarmStatus(buffer.readByte());
        entity.setRunningStatus(buffer.readByte());
        entity.setComponentStatus(buffer.readByte());

        entity.setBatteryVoltage(buffer.readByte());

        entity.setProgramModel(buffer.readByte());
        entity.setProgramState(buffer.readByte());
        entity.setProgramTaskIndex(buffer.readByte());
        entity.setProgramIndex(buffer.readByte());

        entity.setTakeoverState(buffer.readByte());

        buffer.skipBytes(1);//主机地址
        buffer.skipBytes(1);//预留

        entity.setLiquidResidue(buffer.readByte());

        entity.setHighVersion(buffer.readByte());
        entity.setLowerVersion(buffer.readByte());

        entity.setSupplyVoltage(0xFFFF & buffer.readShort());
        buffer.release();
        return entity;
    }
}
