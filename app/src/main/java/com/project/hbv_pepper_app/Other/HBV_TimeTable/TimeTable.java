package com.project.hbv_pepper_app.Other.HBV_TimeTable;

import java.util.List;

public class TimeTable {
    // all public because we need to access all anyway
    public List<Lectures> Mo;
    public List<Lectures> Di;
    public List<Lectures> Mi;
    public List<Lectures> Do;
    public List<Lectures> Fr;
    public String toString() {
        return String.format("Mo: %s,\nDi: %s,\nMi: %s,\nDo: %s,\nFr: %s", Mo.toString(), Di.toString(), Mi.toString(), Do.toString(), Fr.toString());
    }
}
