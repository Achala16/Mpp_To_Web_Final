package com.example.project.config;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class CamelCaseToUppercaseTablesNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {

    private Identifier adjustName(final Identifier name) {
        if (name == null || name.getText() == null || name.getText().isEmpty()) {
            return name;
        }
        final String originalName = name.getText();
        final String adjustedName = Character.toUpperCase(originalName.charAt(0)) + originalName.substring(1);
        return new Identifier(adjustedName, true);
    }

    @Override
    public Identifier toPhysicalTableName(final Identifier name, final JdbcEnvironment context) {
        return adjustName(super.toPhysicalTableName(name, context));
    }
}