/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.auraframework.integration.test.root.component;

import java.util.Collections;
import java.util.Map;

import org.auraframework.def.AttributeDefRef;
import org.auraframework.impl.AuraImplTestCase;
import org.auraframework.impl.expression.PropertyReferenceImpl;
import org.auraframework.instance.AttributeSet;
import org.auraframework.instance.Component;
import org.auraframework.system.Location;
import org.auraframework.throwable.quickfix.AttributeNotFoundException;
import org.auraframework.throwable.quickfix.QuickFixException;
import org.auraframework.util.json.Json;
import org.auraframework.util.json.JsonReader;
import org.junit.Test;

public class ComponentImplTest extends AuraImplTestCase {

    protected final static String baseComponentTag = "<aura:component %s>%s</aura:component>";

    @Test
    public void testComponentAttributeMap() throws Exception {
        Component cmp = vendor.makeComponent("test:child1", "meh");
        AttributeDefRef adr = vendor.makeAttributeDefRef("attr", "some value", new Location("meh", 0));
        cmp.getAttributes().set(Collections.singleton(adr));

        assertEquals(1, cmp.getAttributes().size());

        assertEquals("some value", cmp.getAttributes().getExpression("attr"));

        try {
            AttributeDefRef adr2 = vendor.makeAttributeDefRef("badAttr", "blubber", new Location("meh", 0));
            cmp.getAttributes().set(Collections.singleton(adr2));
            fail("Should have thrown AuraException(Attribute not Defined)");
        } catch (AttributeNotFoundException expected) {
        }
    }

    @Test
    public void testSerialize() throws Exception {
        Component testComponent = vendor.makeComponent("test:parent", "fuh");
        testComponent.getClass();
        serializeAndGoldFile(testComponent);
    }

    /**
     * Component classes will be serialized with the ComponentDef instead of on the Component.
     */
    @Test
    public void testComponentClassesNotSerialized() throws Exception {
        Object cmp = vendor.makeComponent("test:parent", "foo");

        Map<?, ?> json = (Map<?, ?>) new JsonReader().read(toJson(cmp));
        String componentClass = (String) ((Map<?, ?>) json.get(Json.ApplicationKey.VALUE.toString()))
                .get("componentClass");

        assertNull(componentClass);
    }

    @Test
    public void testReinitializeModel() throws Exception {
        Component cmp = vendor.makeComponent("test:child1", "meh");

        PropertyReferenceImpl propRef = new PropertyReferenceImpl("value", null);
        assertEquals(null, cmp.getModel().getValue(propRef));

        setAttribute(cmp, "attr", "some value");

        assertEquals(null, cmp.getModel().getValue(propRef));

        cmp.reinitializeModel();
        assertEquals("some value", cmp.getModel().getValue(propRef));

        setAttribute(cmp, "attr", "some other value");
        cmp.reinitializeModel();
        assertEquals("some other value", cmp.getModel().getValue(propRef));
    }

    private void setAttribute(Component cmp, String name, String value) throws QuickFixException {
        AttributeDefRef adr = vendor.makeAttributeDefRef(name, value, new Location("meh", 0));
        AttributeSet attributes = cmp.getAttributes();
        attributes.set(Collections.singleton(adr));
    }
}
