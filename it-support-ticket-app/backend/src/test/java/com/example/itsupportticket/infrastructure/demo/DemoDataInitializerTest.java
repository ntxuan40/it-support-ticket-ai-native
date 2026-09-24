package com.example.itsupportticket.infrastructure.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

import com.example.itsupportticket.config.DemoDataProperties;
import com.example.itsupportticket.domain.model.DeviceEntity;
import com.example.itsupportticket.domain.model.UserEntity;
import com.example.itsupportticket.domain.repository.DeviceRepository;
import com.example.itsupportticket.domain.repository.TicketRepository;
import com.example.itsupportticket.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class DemoDataInitializerTest {

    @Mock
    private DemoDataProperties demoDataProperties;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private DemoDataInitializer initializer;

    @Test
    void run_shouldSeedDemoData_whenEnabledAndEmpty() {
        when(demoDataProperties.isEnabled()).thenReturn(true);
        when(userRepository.count()).thenReturn(0L);
        when(deviceRepository.count()).thenReturn(0L);
        when(ticketRepository.count()).thenReturn(0L);

        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(deviceRepository.save(any(DeviceEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(userRepository, times(5)).save(any(UserEntity.class));
        verify(deviceRepository, times(3)).save(any(DeviceEntity.class));
        verify(ticketRepository, times(1)).saveAll(any());
    }

    @Test
    void run_shouldSkipSeeding_whenDemoDataDisabled() {
        when(demoDataProperties.isEnabled()).thenReturn(false);

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(userRepository, never()).save(any(UserEntity.class));
        verify(deviceRepository, never()).save(any(DeviceEntity.class));
        verify(ticketRepository, never()).saveAll(any());
    }

    @Test
    void run_shouldSkipSeeding_whenDatabaseAlreadyContainsData() {
        when(demoDataProperties.isEnabled()).thenReturn(true);
        when(userRepository.count()).thenReturn(10L);
        when(deviceRepository.count()).thenReturn(3L);
        when(ticketRepository.count()).thenReturn(2L);

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(userRepository, never()).save(any(UserEntity.class));
        verify(deviceRepository, never()).save(any(DeviceEntity.class));
        verify(ticketRepository, never()).saveAll(any());
    }

    @Test
    void run_shouldNotDuplicateDemoData_whenStartupRunsRepeatedly() {
        when(demoDataProperties.isEnabled()).thenReturn(true);
        when(userRepository.count()).thenReturn(0L, 5L);
        when(deviceRepository.count()).thenReturn(0L, 3L);
        when(ticketRepository.count()).thenReturn(0L, 1L);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(deviceRepository.save(any(DeviceEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        initializer.run(new DefaultApplicationArguments(new String[0]));
        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(userRepository, times(5)).save(any(UserEntity.class));
        verify(deviceRepository, times(3)).save(any(DeviceEntity.class));
        verify(ticketRepository, times(1)).saveAll(any());
    }
}
