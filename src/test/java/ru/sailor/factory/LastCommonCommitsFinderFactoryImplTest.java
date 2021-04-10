package ru.sailor.factory;

import org.junit.Test;

public class LastCommonCommitsFinderFactoryImplTest {

    @Test
    public void testNoErrorsOnCreate() {
        var factory = new LastCommonCommitsFinderFactoryImpl();
        factory.create("invalidOwner", "invalidRepo", "invalidToken");
    }

}