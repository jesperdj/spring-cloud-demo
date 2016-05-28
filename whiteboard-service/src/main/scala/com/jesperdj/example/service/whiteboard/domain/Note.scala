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
package com.jesperdj.example.service.whiteboard.domain

import java.lang.{Long => JavaLong}
import java.util.Date
import javax.persistence.{Column, Entity, GeneratedValue, Id}

import scala.beans.BeanProperty

@Entity
class Note {

  @Id
  @GeneratedValue
  @BeanProperty
  var id: JavaLong = _

  @Column(nullable = false)
  @BeanProperty
  var createdDateTime: Date = _

  @Column(nullable = false, length = 40)
  @BeanProperty
  var authorName: String = _

  @Column(nullable = false, length = 1000)
  @BeanProperty
  var content: String = _
}
