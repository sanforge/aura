<!--

    Copyright (C) 2013 salesforce.com, inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
-->
<aura:application controller="java://org.auraframework.components.test.java.controller.JavaTestController">
    <aura:attribute name="completed" type="Boolean" default="false"/>

    <auraStorage:init name="actions"
                  persistent="false"
                  secure="true"
                  defaultExpiration="900"
                  defaultAutoRefreshInterval="30"
                  maxSize="4096"/>

    <aura:method name="getStringAction" action="{!c.getStringAction}" />
</aura:application>