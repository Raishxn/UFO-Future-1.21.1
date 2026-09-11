package com.raishxn.ufo.crafting;

/** Pure capacity rules for the Singularity's nine-slot pattern pages. */
public final class SingularityPatternCapacity {
    public static final int PATTERNS_PER_PAGE = 9;
    public static final int PAGES_PER_MK1_FIELD = 1;
    public static final int PAGES_PER_MK2_FIELD = 2;
    public static final int PAGES_PER_MK3_FIELD = 4;
    public static final int FIELD_POSITION_COUNT = 25;
    public static final int MAX_PATTERN_SLOTS =
            FIELD_POSITION_COUNT * PAGES_PER_MK3_FIELD * PATTERNS_PER_PAGE;

    private SingularityPatternCapacity() {
    }

    public static int calculate(int mk1Fields, int mk2Fields, int mk3Fields) {
        int pages = Math.max(0, mk1Fields) * PAGES_PER_MK1_FIELD
                + Math.max(0, mk2Fields) * PAGES_PER_MK2_FIELD
                + Math.max(0, mk3Fields) * PAGES_PER_MK3_FIELD;
        return Math.min(MAX_PATTERN_SLOTS, pages * PATTERNS_PER_PAGE);
    }
}
