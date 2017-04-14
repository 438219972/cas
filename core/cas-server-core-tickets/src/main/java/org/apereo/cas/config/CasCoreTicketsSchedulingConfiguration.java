package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.registry.DefaultTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.NoOpTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.ticket.registry.support.LockingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * This is {@link CasCoreTicketsSchedulingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreTicketsSchedulingConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@EnableAsync
@EnableTransactionManagement(proxyTargetClass = true)
@AutoConfigureAfter(CasCoreTicketsConfiguration.class)
public class CasCoreTicketsSchedulingConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasCoreTicketsSchedulingConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "ticketRegistryCleaner")
    @Bean
    @Autowired
    public TicketRegistryCleaner ticketRegistryCleaner(@Qualifier("lockingStrategy") final LockingStrategy lockingStrategy,
                                                       @Qualifier("logoutManager") final LogoutManager logoutManager,
                                                       @Qualifier("ticketRegistry") final TicketRegistry ticketRegistry) {
        final boolean isCleanerEnabled = casProperties.getTicket().getRegistry().getCleaner().isEnabled();
        if (isCleanerEnabled) {
            LOGGER.debug("Default ticket registry cleaner is enabled");
            return new DefaultTicketRegistryCleaner(lockingStrategy, logoutManager,
                    ticketRegistry, isCleanerEnabled);
        }
        return new NoOpTicketRegistryCleaner(lockingStrategy, logoutManager,
                ticketRegistry, isCleanerEnabled);
    }

    @ConditionalOnMissingBean(name = "ticketRegistryCleanerScheduler")
    @Bean
    @Autowired
    public TicketRegistryCleanerScheduler ticketRegistryCleanerScheduler(@Qualifier("ticketRegistryCleaner") 
                                                                         final TicketRegistryCleaner ticketRegistryCleaner) {
        return new TicketRegistryCleanerScheduler(ticketRegistryCleaner);
    }

    public static class TicketRegistryCleanerScheduler {
        private final TicketRegistryCleaner ticketRegistryCleaner;

        public TicketRegistryCleanerScheduler(final TicketRegistryCleaner ticketRegistryCleaner) {
            this.ticketRegistryCleaner = ticketRegistryCleaner;
        }

        @Scheduled(initialDelayString = "${cas.ticket.registry.cleaner.startDelay:20000}",
                fixedDelayString = "${cas.ticket.registry.cleaner.repeatInterval:60000}")
        public void run() {
            this.ticketRegistryCleaner.clean();
        }
    }
}
