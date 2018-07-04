/*
 * Copyright (C) 2015 Orange
 *
 * This software is distributed under the terms and conditions of the 'GNU GENERAL PUBLIC LICENSE
 * Version 2' license which can be found in the file 'LICENSE.txt' in this package distribution or
 * at 'http://www.gnu.org/licenses/gpl-2.0-standalone.html'.
 */

package com.orange.cepheus.broker;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by pborscia on 06/08/2015.
 */

@SpringBootApplication
@ComponentScan("com.orange")
@EnableScheduling
public class Application {

    public static void main(String[] args) {

        new SpringApplicationBuilder()
                .sources(Application.class)
                .showBanner(false)
                .run(args);
    }
}
