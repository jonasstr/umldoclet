/*
 * Copyright 2016-2019 Talsma ICT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.talsmasoftware.umldoclet.uml;

import nl.talsmasoftware.umldoclet.configuration.TypeDisplay;
import nl.talsmasoftware.umldoclet.rendering.indent.IndentingPrintWriter;

import java.util.Objects;

/**
 * @author Sjoerd Talsma
 */
public class Method extends TypeMember {

    public Method(Type containingType, String name, TypeName returnType) {
        super(containingType, name, returnType);
    }

    private Parameters getParameters() {
        return getChildren().stream()
                .filter(Parameters.class::isInstance).map(Parameters.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No method parameters found!")); // TODO: 'no parameters'?
    }

    @Override
    protected <IPW extends IndentingPrintWriter> IPW writeParametersTo(IPW output) {
        return getParameters().writeTo(output);
    }

    @Override
    public <IPW extends IndentingPrintWriter> IPW writeTo(IPW output) {
        if (!getConfiguration().methods().include(getVisibility())) return output;
        return super.writeTo(output);
    }

    @Override
    protected <IPW extends IndentingPrintWriter> IPW writeTypeTo(IPW output) {
        TypeDisplay returnTypeDisplay = getConfiguration().methods().returnType();
        if (type != null && !TypeDisplay.NONE.equals(returnTypeDisplay)) {
            output.append(": ").append(type.toUml(returnTypeDisplay, null));
        }
        return output;
    }

    @Override
    void replaceParameterizedType(TypeName from, TypeName to) {
        super.replaceParameterizedType(from, to);
        getParameters().replaceParameterizedType(from, to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getParameters());
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other)
                && getParameters().equals(((Method) other).getParameters());
    }

}
