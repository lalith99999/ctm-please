package com.ctm.model;

public enum Stadium {
    LORDS_CRICKET_GROUND("Lord's Cricket Ground, London"),
    MELBOURNE_CRICKET_GROUND("Melbourne Cricket Ground, Australia"),
    EDEN_GARDENS("Eden Gardens, Kolkata, India"),
    WANKHEDE_STADIUM("Wankhede Stadium, Mumbai, India"),
    SYDNEY_CRICKET_GROUND("Sydney Cricket Ground, Australia"),
    NEWLANDS("Newlands, Cape Town, South Africa"),
    ADELAIDE_OVAL("Adelaide Oval, Australia"),
    GABBA("The Gabba, Brisbane, Australia"),
    HEADINGLEY("Headingley, Leeds, England"),
    OLD_TRAFFORD("Old Trafford, Manchester, England"),
    SHARJAH_CRICKET_STADIUM("Sharjah Cricket Stadium, UAE"),
    DUBAI_INTERNATIONAL("Dubai International Stadium, UAE"),
    GADDAFI_STADIUM("Gaddafi Stadium, Lahore, Pakistan"),
    NATIONAL_STADIUM_KARACHI("National Stadium, Karachi, Pakistan"),
    QUEENS_PARK_OVAL("Queen’s Park Oval, Trinidad"),
    KENSINGTON_OVAL("Kensington Oval, Barbados"),
    WANDERERS_STADIUM("Wanderers Stadium, Johannesburg, South Africa"),
    BASIN_RESERVE("Basin Reserve, Wellington, New Zealand"),
    EDEN_PARK("Eden Park, Auckland, New Zealand"),
    ARUN_JAITLEY_STADIUM("Arun Jaitley Stadium, Delhi, India");

    private final String fullName;

    Stadium(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }
}
