package com.example.minitfg.utils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SessionManagerTest {

    @Mock
    Context mockContext;
    @Mock
    SharedPreferences mockPrefs;
    @Mock
    SharedPreferences.Editor mockEditor;

    private SessionManager sessionManager;

    @Before
    public void setUp() {
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
        when(mockEditor.putInt(anyString(), anyInt())).thenReturn(mockEditor);
        when(mockEditor.clear()).thenReturn(mockEditor);

        sessionManager = new SessionManager(mockContext);
    }

    @Test
    public void testSaveSession() {
        sessionManager.saveSession("test_token", "test@example.com", 1);
        
        verify(mockEditor).putString("jwt_token", "test_token");
        verify(mockEditor).putString("user_email", "test@example.com");
        verify(mockEditor).putInt("user_id", 1);
        verify(mockEditor).apply();
    }

    @Test
    public void testIsLoggedIn_True() {
        when(mockPrefs.getString("jwt_token", null)).thenReturn("valid_token");
        assertTrue(sessionManager.isLoggedIn());
    }

    @Test
    public void testIsLoggedIn_False() {
        when(mockPrefs.getString("jwt_token", null)).thenReturn(null);
        assertFalse(sessionManager.isLoggedIn());
    }

    @Test
    public void testLogout() {
        sessionManager.logout();
        verify(mockEditor).clear();
        verify(mockEditor).apply();
    }
}
