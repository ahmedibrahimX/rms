package com.example.rms.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAsync(proxyTargetClass = true)
@EnableScheduling
@Profile("!non-async")
public class AsyncConfiguration implements AsyncConfigurer {

  // async config will be applied unless the profile is explicitly defined as non-async

}