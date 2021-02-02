/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.jmap.change

import org.apache.james.core.Username
import org.apache.james.events.Event
import org.apache.james.events.Event.EventId
import org.apache.james.jmap.core.{AccountId, State}

sealed trait TypeName {
  def asMap(maybeState: Option[State]): Map[TypeName, State] =
    maybeState.map(state => Map[TypeName, State](this -> state))
      .getOrElse(Map())
}
case object MailboxTypeName extends TypeName
case object EmailTypeName extends TypeName

case class StateChange(changes: Map[AccountId, TypeState])

case class TypeState(changes: Map[TypeName, State])

case class StateChangeEvent(eventId: EventId,
                            username: Username,
                            mailboxState: Option[State],
                            emailState: Option[State]) extends Event {

  def asStateChange: StateChange =
    StateChange(Map(AccountId.from(username).fold(
      failure => throw new IllegalArgumentException(failure),
      success => success) ->
      TypeState(
        MailboxTypeName.asMap(mailboxState) ++
          EmailTypeName.asMap(emailState))))

  override def getUsername: Username = username

  override def isNoop: Boolean = false

  override def getEventId: EventId = eventId
}