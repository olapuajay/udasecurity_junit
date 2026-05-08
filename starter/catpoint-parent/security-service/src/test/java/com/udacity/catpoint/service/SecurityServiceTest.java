package com.udacity.catpoint.service;

import com.udacity.catpoint.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {
    @Mock
    private SecurityRepository securityRepository;

    @Mock
    private ImageService imageService;

    private SecurityService securityService;

    @BeforeEach
    void setUp() {
        securityService = new SecurityService(securityRepository, imageService);
    }

    @Test
    void sensorActivated_armedHome_pendingAlarm() {
        Sensor sensor = new Sensor("Front Door", SensorType.DOOR);

        when(securityRepository.getArmingStatus())
                .thenReturn(ArmingStatus.ARMED_HOME);

        when(securityRepository.getAlarmStatus())
                .thenReturn(AlarmStatus.NO_ALARM);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository)
                .setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @Test
    void catDetected_armedHome_alarmState() {
        when(securityRepository.getArmingStatus())
                .thenReturn(ArmingStatus.ARMED_HOME);

        when(imageService.imageContainsCat(any(), anyFloat()))
                .thenReturn(true);

        securityService.processImage(null);

        verify(securityRepository)
                .setAlarmStatus(AlarmStatus.ALARM);
    }

    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class,
            names = {"ARMED_HOME", "ARMED_AWAY"})
    void armedSensorActivated_pendingAlarm(ArmingStatus status) {
        Sensor sensor = new Sensor("Door", SensorType.DOOR);

        when(securityRepository.getArmingStatus())
                .thenReturn(status);

        when(securityRepository.getAlarmStatus())
                .thenReturn(AlarmStatus.NO_ALARM);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository)
                .setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @Test
    void sensorActivated_pendingAlarm_alarm() {
        Sensor sensor = new Sensor("Door", SensorType.DOOR);

        when(securityRepository.getArmingStatus())
                .thenReturn(ArmingStatus.ARMED_HOME);

        when(securityRepository.getAlarmStatus())
                .thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository)
                .setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void sensorActivated_disarmed_noAlarmChange() {
        Sensor sensor = new Sensor("Door", SensorType.DOOR);

        when(securityRepository.getArmingStatus())
                .thenReturn(ArmingStatus.DISARMED);
        when(securityRepository.getAlarmStatus())
                .thenReturn(AlarmStatus.NO_ALARM);

        securityService.changeSensorActivationStatus(sensor, true);
        verify(securityRepository).updateSensor(sensor);
    }

    @Test
    void sensorDeactivated_pendingAlarm_noAlarm() {
        Sensor sensor = new Sensor("Door", SensorType.DOOR);
        sensor.setActive(true);

        when(securityRepository.getAlarmStatus())
                .thenReturn(AlarmStatus.PENDING_ALARM);
        when(securityRepository.getSensors())
                .thenReturn(Set.of(sensor));

        securityService.changeSensorActivationStatus(sensor, false);

        verify(securityRepository)
                .setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void sensorChanged_alarmState_noChange() {
        Sensor sensor = new Sensor("Door", SensorType.DOOR);

        when(securityRepository.getAlarmStatus())
                .thenReturn(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository)
                .updateSensor(sensor);
    }

    @Test
    void activeSensorActivated_pendingAlarm_alarm() {
        Sensor sensor = new Sensor("Door", SensorType.DOOR);
        sensor.setActive(true);

        when(securityRepository.getAlarmStatus())
                .thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository)
                .setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void inactiveSensorDeactivated_noChange() {
        Sensor sensor = new Sensor("Door", SensorType.DOOR);

        when(securityRepository.getAlarmStatus())
                .thenReturn(AlarmStatus.NO_ALARM);

        securityService.changeSensorActivationStatus(sensor, false);

        verify(securityRepository)
                .updateSensor(sensor);
    }

    @Test
    void noCat_noActiveSensors_noAlarm() {
        when(imageService.imageContainsCat(any(), anyFloat()))
                .thenReturn(false);
        when(securityRepository.getSensors())
                .thenReturn(Set.of());

        securityService.processImage(null);

        verify(securityRepository)
                .setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void noCat_activeSensors_alarmUnchanged() {
        Sensor sensor = new Sensor("Door", SensorType.DOOR);
        sensor.setActive(true);

        when(imageService.imageContainsCat(any(), anyFloat()))
                .thenReturn(false);
        when(securityRepository.getSensors())
                .thenReturn(Set.of(sensor));

        securityService.processImage(null);

        verify(securityRepository, org.mockito.Mockito.never())
                .setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void disarmed_noAlarm() {
        securityService.setArmingStatus(ArmingStatus.DISARMED);

        verify(securityRepository)
                .setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void armed_resetsSensors() {
        Sensor sensor = new Sensor("Door", SensorType.DOOR);
        sensor.setActive(true);

        when(securityRepository.getSensors())
                .thenReturn(Set.of(sensor));

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        verify(securityRepository)
                .updateSensor(sensor);
    }

    @Test
    void armedHome_catDetected_alarm() {
        when(imageService.imageContainsCat(any(), anyFloat()))
                .thenReturn(true);
        when(securityRepository.getArmingStatus())
                .thenReturn(ArmingStatus.DISARMED);

        securityService.processImage(null);

        when(securityRepository.getSensors())
                .thenReturn(Set.of());

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        verify(securityRepository)
                .setAlarmStatus(AlarmStatus.ALARM);
    }
}
