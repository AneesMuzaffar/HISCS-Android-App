package com.alphamstudios.hiscs;

public interface HISCSInterface {
    void setPriority(String p);
    void setSwitch(String s);
    void setStatus(String s);
    void recalculate();
    void addSchedule(String s);
    void cancelAllSchedules();
    void refreshData();
}
