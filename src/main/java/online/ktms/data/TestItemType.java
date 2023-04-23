package online.ktms.data;

import lombok.Getter;

public enum TestItemType {

    TEST_CASE("Test case"), TEST_SUITE("Test suite");


    @Getter
    private String label;

    TestItemType(String label) {
        this.label = label;
    }
}
