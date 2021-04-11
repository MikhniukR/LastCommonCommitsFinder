package ru.sailor.factory;

import org.junit.Test;

public class LastCommonCommitsFinderFactoryImplTest {

    @Test
    public void testNoValidation() {
        var factory = new LastCommonCommitsFinderFactoryImpl();
        factory.create("invalidOwner", "invalidRepo", "invalidToken");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBlankOwner() {
        var factory = new LastCommonCommitsFinderFactoryImpl();
        factory.create("invalidOwner", " ", "invalidToken");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBlankRepo() {
        var factory = new LastCommonCommitsFinderFactoryImpl();
        factory.create("invalidOwner", " ", "invalidToken");
    }

}