package com.example.mahila;

import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ProfileAvatarTest {

    private static class MockPreferencesStore {
        private final Map<String, String> map = new HashMap<>();

        public void saveProfileImagePath(String email, String path) {
            if (email == null || email.isEmpty()) return;
            map.put("profile_image_path_" + email, path);
        }

        public String getProfileImagePath(String email) {
            if (email == null || email.isEmpty()) return "";
            return map.getOrDefault("profile_image_path_" + email, "");
        }

        public void clearProfileImagePath(String email) {
            if (email == null || email.isEmpty()) return;
            map.remove("profile_image_path_" + email);
        }
    }

    @Test
    public void testProfileImagePathPersistenceAndUserIsolation() {
        MockPreferencesStore prefs = new MockPreferencesStore();

        String user1 = "anjali@example.com";
        String user2 = "priya@example.com";
        String path1 = "/data/user/0/com.example.mahila/files/avatar_anjali_example_com.jpg";
        String path2 = "/data/user/0/com.example.mahila/files/avatar_priya_example_com.jpg";

        prefs.saveProfileImagePath(user1, path1);
        prefs.saveProfileImagePath(user2, path2);

        assertEquals(path1, prefs.getProfileImagePath(user1));
        assertEquals(path2, prefs.getProfileImagePath(user2));
        assertNotEquals(prefs.getProfileImagePath(user1), prefs.getProfileImagePath(user2));
    }

    @Test
    public void testProfileImageFallbackWhenMissing() {
        MockPreferencesStore prefs = new MockPreferencesStore();
        String user = "unknown@example.com";

        String savedPath = prefs.getProfileImagePath(user);
        assertEquals("", savedPath);

        boolean exists = savedPath != null && !savedPath.isEmpty() && new File(savedPath).exists();
        assertFalse(exists);
    }

    @Test
    public void testProfileImageClear() {
        MockPreferencesStore prefs = new MockPreferencesStore();
        String user = "kavya@example.com";
        String path = "/data/user/0/com.example.mahila/files/avatar_kavya.jpg";

        prefs.saveProfileImagePath(user, path);
        assertEquals(path, prefs.getProfileImagePath(user));

        prefs.clearProfileImagePath(user);
        assertEquals("", prefs.getProfileImagePath(user));
    }
}
