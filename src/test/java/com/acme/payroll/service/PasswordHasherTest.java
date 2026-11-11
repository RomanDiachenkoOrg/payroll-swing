package com.acme.payroll.service;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class PasswordHasherTest {

    @Test
    public void verifiesCorrectPasswordOnly() {
        String hash = PasswordHasher.hash("Passw0rd!".toCharArray());
        assertTrue(hash.startsWith("pbkdf2$"));
        assertTrue(PasswordHasher.verify("Passw0rd!".toCharArray(), hash));
        assertFalse(PasswordHasher.verify("passw0rd!".toCharArray(), hash));
        assertFalse(PasswordHasher.verify("Passw0rd!".toCharArray(), null));
        assertFalse(PasswordHasher.verify("Passw0rd!".toCharArray(), "plain-text"));
    }

    @Test
    public void saltMakesHashesDifferent() {
        assertNotEquals(PasswordHasher.hash("x".toCharArray()), PasswordHasher.hash("x".toCharArray()));
    }
}
