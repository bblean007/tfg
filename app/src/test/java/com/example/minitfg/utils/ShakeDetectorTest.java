package com.example.minitfg.utils;

import static org.mockito.Mockito.*;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;

public class ShakeDetectorTest {

    private ShakeDetector shakeDetector;
    private ShakeDetector.OnShakeListener mockListener;

    @Before
    public void setUp() {
        shakeDetector = new ShakeDetector();
        mockListener = mock(ShakeDetector.OnShakeListener.class);
        shakeDetector.setOnShakeListener(mockListener);
    }

    @Test
    public void testShakeDetection_HighGForce() throws Exception {
        // Create a mock SensorEvent
        SensorEvent event = mock(SensorEvent.class);
        
        // SensorEvent.values is a public field, but Mockito might have trouble mocking it directly
        // We can use reflection to set the values if needed, but let's try setting the field if it's accessible
        float[] values = new float[]{0, 20f, 0}; // High Y acceleration (~2G)
        
        Field valuesField = SensorEvent.class.getField("values");
        valuesField.setAccessible(true);
        valuesField.set(event, values);

        // Trigger the sensor change
        shakeDetector.onSensorChanged(event);

        // Verify the listener was called
        verify(mockListener, atLeastOnce()).onShake(anyInt());
    }

    @Test
    public void testShakeDetection_LowGForce() throws Exception {
        SensorEvent event = mock(SensorEvent.class);
        float[] values = new float[]{0, 9.8f, 0}; // Normal gravity
        
        Field valuesField = SensorEvent.class.getField("values");
        valuesField.setAccessible(true);
        valuesField.set(event, values);

        shakeDetector.onSensorChanged(event);

        // Verify the listener was NOT called
        verify(mockListener, never()).onShake(anyInt());
    }
}
