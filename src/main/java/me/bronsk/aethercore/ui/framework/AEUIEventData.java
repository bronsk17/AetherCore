package me.bronsk.aethercore.ui.framework;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.simple.StringCodec;
import com.hypixel.hytale.codec.util.RawJsonReader;
import org.bson.BsonValue;

import java.io.IOException;

/** Generic (action, value) event payload decoded from every native Custom UI interaction. */
public final class AEUIEventData {

    public static final String KEY_ACTION = "Action";
    public static final String KEY_VALUE = "@Value";

    /**
     * Native elements report {@code @Value} in whatever type matches their own property - a string
     * for a text field, a real boolean for a CheckBox - so a plain {@link Codec#STRING} can't
     * decode it, on either wire format this event system uses: {@code BsonValue.asString()} on a
     * BsonBoolean throws in the BSON path, and {@code RawJsonReader.readString()} chokes on an
     * unquoted {@code true}/{@code false} literal in the raw-JSON path (the one actually used for
     * live client events - confirmed via server stack trace). Either way {@link BuilderCodec} has
     * no per-field try/catch, so a decode failure silently drops the whole event, action included.
     * Every consumer of {@link #getValue()} already treats it as text (e.g. parsing it back with
     * {@code Boolean.parseBoolean} for a checkbox), so extending the real {@link StringCodec} and
     * special-casing booleans on both paths is enough - everything else (encode, schema) still
     * comes from the proven parent implementation.
     */
    private static final Codec<String> VALUE_CODEC = new StringCodec() {
        @Override
        public String decode(BsonValue bsonValue, ExtraInfo extraInfo) {
            return bsonValue.isBoolean() ? String.valueOf(bsonValue.asBoolean().getValue()) : super.decode(bsonValue, extraInfo);
        }

        @Override
        public String decodeJson(RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
            int next = reader.peek();
            boolean isBooleanLiteral = next == 't' || next == 'T' || next == 'f' || next == 'F';
            return isBooleanLiteral ? String.valueOf(reader.readBooleanValue()) : super.decodeJson(reader, extraInfo);
        }
    };

    public static final BuilderCodec<AEUIEventData> CODEC = BuilderCodec.builder(AEUIEventData.class, AEUIEventData::new)
            .addField(new KeyedCodec<>(KEY_ACTION, Codec.STRING), AEUIEventData::setAction, AEUIEventData::getAction)
            .addField(new KeyedCodec<>(KEY_VALUE, VALUE_CODEC), AEUIEventData::setValue, AEUIEventData::getValue)
            .build();

    private String action;
    private String value;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
