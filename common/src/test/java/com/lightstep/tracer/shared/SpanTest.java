package com.lightstep.tracer.shared;

import com.lightstep.tracer.grpc.KeyValue;
import com.lightstep.tracer.grpc.Log;
import com.lightstep.tracer.grpc.Span.Builder;
import io.opentracing.tag.BooleanTag;
import io.opentracing.tag.StringTag;
import io.opentracing.tag.Tag;
import io.opentracing.tag.Tags;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static com.lightstep.tracer.shared.Span.LOG_KEY_EVENT;
import static com.lightstep.tracer.shared.Span.LOG_KEY_MESSAGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpanTest {
    private static final long TRACE_ID = 1;
    private static final long SPAN_ID = 2;

    private SpanContext spanContext;
    private Builder grpcSpan;

    @Mock
    private AbstractTracer abstractTracer;

    private Span undertest;

    @Before
    public void setup() {
        spanContext = new SpanContext(TRACE_ID, SPAN_ID, null);
        grpcSpan = com.lightstep.tracer.grpc.Span.newBuilder();
        undertest = new Span(abstractTracer, spanContext, grpcSpan, 0L);
    }

    @Test
    public void testContext() {
        assertSame(spanContext, undertest.context());
    }

    @Test
    public void testFinish_withoutFinishTime() {
        assertEquals(0L, grpcSpan.getDurationMicros());
        undertest.finish();
        assertNotEquals(0L, grpcSpan.getDurationMicros());
        verify(abstractTracer).addSpan(grpcSpan.build());
    }

    @Test
    public void testFinish_withFinishTime() {
        long finishTimeMicros = 123L;
        undertest.finish(finishTimeMicros);
        assertEquals(finishTimeMicros, grpcSpan.getDurationMicros());
        verify(abstractTracer).addSpan(grpcSpan.build());
    }

    @Test
    public void testSetTag_stringTypeNullValue() {
        Span result = undertest.setTag("k", (String) null);
        assertSame(result, undertest);
        verify(abstractTracer).debug("key (k) or value (null) is null, ignoring");
        assertTrue("When value is null, should not be added to attributes", grpcSpan.getTagsList().isEmpty());
    }

    @Test
    public void testSetTag_stringTypeNullKey() {
        Span result = undertest.setTag((String)null, "v");
        assertSame(result, undertest);
        verify(abstractTracer).debug("key (null) or value (v) is null, ignoring");
        assertTrue("When key is null, should not be added to attributes", grpcSpan.getTagsList().isEmpty());
    }

    @Test
    public void testSetTag_stringType() {
        Span result = undertest.setTag("a-key", "v");
        assertSame(result, undertest);
        verifyZeroInteractions(abstractTracer);
        assertNotNull(grpcSpan.getTagsList());
        assertEquals(1, grpcSpan.getTagsCount());
        assertEquals(KeyValue.newBuilder().setKey("a-key").setStringValue("v").build(), grpcSpan.getTags(0));

        result = undertest.setTag(new StringTag("b-key"), "v");
        assertSame(result, undertest);
        verifyZeroInteractions(abstractTracer);
        assertNotNull(grpcSpan.getTagsList());
        assertEquals(2, grpcSpan.getTagsCount());
        assertEquals(KeyValue.newBuilder().setKey("b-key").setStringValue("v").build(), grpcSpan.getTags(1));
    }

    @Test
    public void testSetTag_booleanTypeNullKey() {
        Span result = undertest.setTag(null, false);
        assertSame(result, undertest);
        verify(abstractTracer).debug("key is null, ignoring");
        assertTrue("When key is null, should not be added to attributes", grpcSpan.getTagsList().isEmpty());
    }

    @Test
    public void testSetTag_booleanType() {
        Span result = undertest.setTag("a-key", true);
        assertSame(result, undertest);
        verifyZeroInteractions(abstractTracer);
        assertNotNull(grpcSpan.getTagsList());
        assertEquals(1, grpcSpan.getTagsCount());
        assertEquals(KeyValue.newBuilder().setKey("a-key").setBoolValue(true).build(), grpcSpan.getTags(0));

        result = undertest.setTag(new BooleanTag("b-key"), true);
        assertSame(result, undertest);
        verifyZeroInteractions(abstractTracer);
        assertNotNull(grpcSpan.getTagsList());
        assertEquals(2, grpcSpan.getTagsCount());
        assertEquals(KeyValue.newBuilder().setKey("b-key").setBoolValue(true).build(), grpcSpan.getTags(1));

    }

    @Test
    public void testSetTag_numberTypeNullValue() {
        Span result = undertest.setTag("k", (Number) null);
        assertSame(result, undertest);
        verify(abstractTracer).debug("key (k) or value (null) is null, ignoring");
        assertTrue("When value is null, should not be added to attributes", grpcSpan.getTagsList().isEmpty());
    }

    @Test
    public void testSetTag_numberTypeNullKey() {
        Span result = undertest.setTag((String)null, 1);
        assertSame(result, undertest);
        verify(abstractTracer).debug("key (null) or value (1) is null, ignoring");
        assertTrue("When key is null, should not be added to attributes", grpcSpan.getTagsList().isEmpty());
    }

    @Test
    public void testSetTag_numberType() {
        Span result = undertest.setTag("a-key", 3);
        assertSame(result, undertest);
        verifyZeroInteractions(abstractTracer);
        assertNotNull(grpcSpan.getTagsList());
        assertEquals(1, grpcSpan.getTagsCount());
        assertEquals(KeyValue.newBuilder().setKey("a-key").setIntValue(3).build(), grpcSpan.getTags(0));
    }

    @Test
    public void testSetTag_TagNull() {
        Span result = undertest.setTag((Tag)null, 1);
        assertSame(result, undertest);
        verify(abstractTracer).debug("tag (null) or value (1) is null, ignoring");
        assertTrue("When the tag is null, should not be added to attributes", grpcSpan.getTagsList().isEmpty());
    }

    @Test
    public void testSetTag_Tag() {
        Span result = undertest.setTag(Tags.COMPONENT, "mytest");
        assertSame(result, undertest);
        verifyZeroInteractions(abstractTracer);
        assertNotNull(grpcSpan.getTagsList());
        assertEquals(1, grpcSpan.getTagsCount());
        assertEquals(KeyValue.newBuilder().setKey(Tags.COMPONENT.getKey()).setStringValue("mytest").build(),
                grpcSpan.getTags(0));
    }

    @Test
    public void testGetBaggageItem() {
        // returns null when no baggage
        assertNull(undertest.getBaggageItem("a-key"));

        // returns value if found in baggage
        spanContext = spanContext.withBaggageItem("a-key", "v");
        undertest = new Span(abstractTracer, spanContext, grpcSpan, 0L);
        assertEquals("v", undertest.getBaggageItem("a-key"));

        // returns null when baggage exists, but key is missing
        assertNull(undertest.getBaggageItem("bogus"));
    }

    @Test
    public void testSetBaggageItem() {
        Span result = undertest.setBaggageItem("a-key", "v");
        assertSame(result, undertest);
        assertEquals("v", undertest.getBaggageItem("a-key"));
        assertNotSame(spanContext, undertest.context());
    }

    @Test
    public void testTraceIdentifiers() {
        assertEquals(String.valueOf(TRACE_ID), undertest.context().toTraceId());
        assertEquals(String.valueOf(SPAN_ID), undertest.context().toSpanId());
    }

    @Test
    public void testSetOperationName() {
        Span result = undertest.setOperationName("my-operation");
        assertSame(result, undertest);
        assertEquals("my-operation", grpcSpan.getOperationName());
    }

    @Test
    public void testSetComponentName() {
        Span result = undertest.setComponentName("custom");
        assertSame(result, undertest);
        verifyZeroInteractions(abstractTracer);

        assertNotNull(grpcSpan.getTagsList());
        assertEquals(1, grpcSpan.getTagsCount());
        assertEquals(KeyValue.newBuilder().setKey(LightStepConstants.Tags.COMPONENT_NAME_KEY).setStringValue("custom").build(), grpcSpan.getTags(0));
    }

    @Test
    public void testSetComponentName_withNullValue() {
        Span result = undertest.setComponentName(null);
        assertSame(result, undertest);
        verify(abstractTracer).debug("componentName is null, ignoring");
        verifyZeroInteractions(abstractTracer);
    }

    @Test
    public void testClose() {
        assertEquals(0L, grpcSpan.getDurationMicros());
        undertest.close();
        assertNotEquals(0L, grpcSpan.getDurationMicros());
        verify(abstractTracer).addSpan(grpcSpan.build());
    }

    private Map<String, String> getLogFieldMap(Log rec) {
        Map<String, String> rval = new HashMap<>();
        for (KeyValue kv : rec.getFieldsList()) {
            rval.put(kv.getKey(), kv.getStringValue());
        }
        return rval;
    }

    @Test
    public void testLog_fieldsOnly_eventProvided() {
        Map<String, String> fields = new HashMap<>();
        fields.put(LOG_KEY_EVENT, "my-key-event");
        fields.put("foo", "bar");

        Span result = undertest.log(fields);

        assertSame(result, undertest);
        assertEquals(1, grpcSpan.getLogsCount());
        Log logRecord = grpcSpan.getLogs(0);
        Map<String, String> fieldMap = getLogFieldMap(logRecord);
        assertEquals("my-key-event", fieldMap.get(LOG_KEY_EVENT));
        assertEquals("bar", fieldMap.get("foo"));
        assertNull(fieldMap.get("payload"));
        assertNotNull(logRecord.getTimestamp());
    }

    @Test
    public void testLog_fieldsOnly_messageProvided() {
        Map<String, String> fields = new HashMap<>();
        fields.put(LOG_KEY_MESSAGE, "my-key-message");
        fields.put("foo", "bar");

        Span result = undertest.log(fields);

        assertSame(result, undertest);
        assertEquals(1, grpcSpan.getLogsCount());
        Log logRecord = grpcSpan.getLogs(0);
        Map<String, String> fieldMap = getLogFieldMap(logRecord);
        assertEquals("my-key-message", fieldMap.get(LOG_KEY_MESSAGE));
        assertEquals("bar", fieldMap.get("foo"));
        assertNotNull(logRecord.getTimestamp());
    }

    @Test
    public void testLog_fieldsOnly_noEventOrMessageProvided() {
        Map<String, String> fields = new HashMap<>();
        fields.put("foo", "bar");

        Span result = undertest.log(fields);

        assertSame(result, undertest);
        assertEquals(1, grpcSpan.getLogsCount());
        Log logRecord = grpcSpan.getLogs(0);
        Map<String, String> fieldMap = getLogFieldMap(logRecord);
        assertNull(fieldMap.get(LOG_KEY_MESSAGE));
        assertEquals("bar", fieldMap.get("foo"));
        assertNotNull(logRecord.getTimestamp());
    }

    @Test
    public void testLog_timeAndFields_eventProvided() {
        Map<String, String> fields = new HashMap<>();
        fields.put(LOG_KEY_EVENT, "my-key-event");
        fields.put("foo", "bar");

        Span result = undertest.log(100L, fields);

        assertSame(result, undertest);
        assertEquals(1, grpcSpan.getLogsCount());
        Log logRecord = grpcSpan.getLogs(0);
        Map<String, String> fieldMap = getLogFieldMap(logRecord);
        assertEquals("my-key-event", fieldMap.get(LOG_KEY_EVENT));
        assertEquals("bar", fieldMap.get("foo"));
        assertEquals(100L, Util.protoTimeToEpochMicros(logRecord.getTimestamp()));
    }

    @Test
    public void testLog_timeAndFields_messageProvided() {
        Map<String, String> fields = new HashMap<>();
        fields.put(LOG_KEY_MESSAGE, "my-key-message");
        fields.put("foo", "bar");

        Span result = undertest.log(100L, fields);

        assertSame(result, undertest);
        assertEquals(1, grpcSpan.getLogsCount());
        Log logRecord = grpcSpan.getLogs(0);
        Map<String, String> fieldMap = getLogFieldMap(logRecord);
        assertEquals("my-key-message", fieldMap.get(LOG_KEY_MESSAGE));
        assertEquals("bar", fieldMap.get("foo"));
        assertEquals(100L, Util.protoTimeToEpochMicros(logRecord.getTimestamp()));
    }

    @Test
    public void testLog_timeAndFields_noEventOrMessageProvided() {
        Map<String, String> fields = new HashMap<>();
        fields.put("foo", "bar");

        Span result = undertest.log(100L, fields);

        assertSame(result, undertest);
        assertEquals(1, grpcSpan.getLogsCount());
        Log logRecord = grpcSpan.getLogs(0);
        Map<String, String> fieldMap = getLogFieldMap(logRecord);
        assertNull(fieldMap.get(LOG_KEY_MESSAGE));
        assertEquals("bar", fieldMap.get("foo"));
        assertEquals(100L, Util.protoTimeToEpochMicros(logRecord.getTimestamp()));
    }

    @Test
    public void testLog_messageOnly() {
        Span result = undertest.log("my message");
        assertSame(result, undertest);
        assertEquals(1, grpcSpan.getLogsCount());
        Log logRecord = grpcSpan.getLogs(0);
        Map<String, String> fieldMap = getLogFieldMap(logRecord);
        assertEquals("my message", fieldMap.get("message"));
        assertNull(fieldMap.get("payload"));
        assertNotNull(logRecord.getTimestamp());
    }

    @Test
    public void testLog_timeAndMessage() {
        Span result = undertest.log(100L, "my message");
        assertSame(result, undertest);
        assertEquals(1, grpcSpan.getLogsCount());
        Log logRecord = grpcSpan.getLogs(0);
        Map<String, String> fieldMap = getLogFieldMap(logRecord);
        assertEquals("my message", fieldMap.get("message"));
        assertNull(fieldMap.get("payload"));
        assertEquals(100L, Util.protoTimeToEpochMicros(logRecord.getTimestamp()));
    }

    @Test
    public void testLog_nullMapKey() {
        Map<String, String> fields = new HashMap<>();
        fields.put(null, "myvalue1");

        Span result = undertest.log(fields);
        Log logRecord = grpcSpan.getLogs(0);
        Map<String, String> fieldMap = getLogFieldMap(logRecord);
        assertEquals(null, fieldMap.get(null));
    }

    @Test
    public void testLog_nullMapValue() {
        Map<String, String> fields = new HashMap<>();
        fields.put("mykey1", null);

        Span result = undertest.log(fields);
        Log logRecord = grpcSpan.getLogs(0);
        Map<String, String> fieldMap = getLogFieldMap(logRecord);
        assertEquals("", fieldMap.get("mykey1"));
    }

    @Test
    public void testGenerateTraceURL() {
        String expecteResult = "https://something.com/";
        when(abstractTracer.generateTraceURL(SPAN_ID)).thenReturn(expecteResult);
        String result = undertest.generateTraceURL();

        assertEquals(expecteResult, result);
    }

    @Test
    public void testStringToJSONValue() {
        assertEquals("\"\\\"quoted\\\"\"", Span.stringToJSONValue("\"quoted\""));
        assertEquals("\"\\\\back-slashed\\\\\"", Span.stringToJSONValue("\\back-slashed\\"));
        assertEquals("\"\\/fwd-slashed\\/\"", Span.stringToJSONValue("/fwd-slashed/"));
        assertEquals("\"\\ttabbed\"", Span.stringToJSONValue("\ttabbed"));
        assertEquals("\"\\bbackspace\"", Span.stringToJSONValue("\bbackspace"));
        assertEquals("\"\\nnewline\"", Span.stringToJSONValue("\nnewline"));
        assertEquals("\"\\rreturn\"", Span.stringToJSONValue("\rreturn"));
        assertEquals("\"\\ffeed\"", Span.stringToJSONValue("\ffeed"));
    }

    @Test
    public void testIds() {
        assertEquals("1", undertest.context().toTraceId());
        assertEquals("2", undertest.context().toSpanId());
    }
//
//    @Test
//    public void testMetaEventLoggingEnabled() {
//        abstractTracer.metaEventLoggingEnabled = true;
//        when(abstractTracer.buildSpan(anyString())).thenReturn(new com.lightstep.tracer.shared.SpanBuilder("", abstractTracer));
//        new Span(abstractTracer, spanContext, grpcSpan, 0L);
//    }
}
