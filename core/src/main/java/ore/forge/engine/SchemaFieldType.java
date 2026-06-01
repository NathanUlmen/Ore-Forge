package ore.forge.engine;

public enum SchemaFieldType {
    //Reference Types:
    ASSET_REF,
    SCHEMA_REF,
    //Primitives
    STRING,
    INT,
    FLOAT,
    BOOL,
    //Complex
    OBJECT,
    LIST,
    ENUM,
    //Math Types
    VECTOR2,
    VECTOR3,
    VECTOR4,
    MATRIX4,
    QUATERNION,
    EQUATION,
    BOOL_EXPRESSION;


    public static SchemaFieldType fromString(String str) {
        return SchemaFieldType.valueOf(str.toUpperCase());
    }
}
