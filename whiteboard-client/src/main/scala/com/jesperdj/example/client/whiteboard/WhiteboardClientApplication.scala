/*
 * Copyright 2016 Jesper de Jong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jesperdj.example.client.whiteboard

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.format.FormatterRegistry
import org.springframework.format.datetime.DateFormatter
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

@SpringBootApplication
@EnableDiscoveryClient
class WhiteboardClientApplication extends WebMvcConfigurerAdapter {

  @Bean
  @LoadBalanced
  def restTemplate: RestTemplate = new RestTemplate()

  @Bean
  def dateFormatter: DateFormatter = new DateFormatter("dd-MM-yyyy HH:mm:ss")

  override def addFormatters(registry: FormatterRegistry): Unit = registry.addFormatter(dateFormatter)
}

object WhiteboardClientApplication extends App {
  SpringApplication.run(classOf[WhiteboardClientApplication], args: _*)
}
