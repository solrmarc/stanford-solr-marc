package org.marc4j.marc.impl;

import java.util.*;

import org.marc4j.marc.*;

/**
 * Represents a MARC record - but without the sorting of the marc fields.
 * 
 * @author Bas Peters
 * @version $Revision: 1.4 $
 */
public class RecordImplRenamed implements Record {

    private Long id;

    private Leader leader;

    private List<ControlField> controlFields;

    private List<DataField> dataFields;

    private String type;

    /**
     * Creates a new <code>Record</code>.
     */
    public RecordImplRenamed() {
        controlFields = new ArrayList<ControlField>();
        dataFields = new ArrayList<DataField>();
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setLeader(Leader leader) {
        this.leader = leader;
    }

    public Leader getLeader() {
        return leader;
    }

    /**
     * Adds a <code>VariableField</code> being a <code>ControlField</code>
     * or <code>DataField</code>.
     * 
     * If the <code>VariableField</code> is a control number field (001) and
     * the record already has a control number field, the field is replaced with
     * the new instance.
     * 
     * @param field
     *            the <code>VariableField</code>
     * @throws IllegalAddException
     *             when the parameter is not a <code>VariableField</code>
     *             instance
     */
    public void addVariableField(VariableField field) {
        if (!(field instanceof VariableField))
            throw new IllegalAddException("Expected VariableField instance");

        String tag = field.getTag();
        if (Verifier.isControlNumberField(tag)) {
            if (Verifier.hasControlNumberField(controlFields))
                controlFields.set(0, (ControlField) field);
            else
                controlFields.add(0, (ControlField) field);
//            Collections.sort(controlFields);
        } else if (Verifier.isControlField(tag)) {
            controlFields.add((ControlField) field);
//            Collections.sort(controlFields);
        } else {
            dataFields.add((DataField) field);
//            Collections.sort(dataFields);
        }

    }

    public void removeVariableField(VariableField field) {
        String tag = field.getTag();
        if (Verifier.isControlField(tag))
            controlFields.remove(field);
        else
            dataFields.remove(field);
    }

    /**
     * Returns the control number field or <code>null</code> if no control
     * number field is available.
     * 
     * @return ControlField - the control number field
     */
    public ControlField getControlNumberField() {
        if (Verifier.hasControlNumberField(controlFields))
            return (ControlField) controlFields.get(0);
        else
            return null;
    }

    public List<ControlField> getControlFields() {
        return controlFields;
    }

    public List<DataField> getDataFields() {
        return dataFields;
    }

    public VariableField getVariableField(String tag) {
        Iterator i;
        if (Verifier.isControlField(tag))
            i = controlFields.iterator();
        else
            i = dataFields.iterator();
        while (i.hasNext()) {
            VariableField field = (VariableField) i.next();
            if (field.getTag().equals(tag))
                return field;
        }
        return null;
    }

    public List<VariableField> getVariableFields(String tag) {
        List<VariableField> fields = new ArrayList<VariableField>();
        Iterator i;
        if (Verifier.isControlField(tag))
            i = controlFields.iterator();
        else
            i = dataFields.iterator();
        while (i.hasNext()) {
            VariableField field = (VariableField) i.next();
            if (field.getTag().equals(tag))
                fields.add(field);
        }
        return fields;
    }

    public List<VariableField> getVariableFields() {
        List<VariableField> fields = new ArrayList<VariableField>();
        Iterator<ControlField> icf = controlFields.iterator();
        while (icf.hasNext())
            fields.add((VariableField) icf.next());
        
        Iterator<DataField> idf = dataFields.iterator();
        while (idf.hasNext())
            fields.add((VariableField) idf.next());
        return fields;
    }

    public String getControlNumber() {
        return new String(getControlNumberField().getData());
    }

    public List<VariableField> getVariableFields(String[] tags) {
        List<VariableField> list = new ArrayList<VariableField>();
        for (int i = 0; i < tags.length; i++) {
            String tag = tags[i];
            List<VariableField> fields = getVariableFields(tag);
            if (fields.size() > 0)
                list.addAll(fields);
        }
        return list;
    }

    /**
     * Returns a string representation of this record.
     * 
     * <p>
     * Example:
     * 
     * <pre>
     *     
     *      LEADER 00714cam a2200205 a 4500 
     *      001 12883376 
     *      005 20030616111422.0
     *      008 020805s2002 nyu j 000 1 eng 
     *      020   $a0786808772 
     *      020   $a0786816155 (pbk.) 
     *      040   $aDLC$cDLC$dDLC 
     *      100 1 $aChabon, Michael. 
     *      245 10$aSummerland /$cMichael Chabon. 
     *      250   $a1st ed. 
     *      260   $aNew York :$bMiramax Books/Hyperion Books for Children,$cc2002. 
     *      300   $a500 p. ;$c22 cm. 
     *      650  1$aFantasy. 
     *      650  1$aBaseball$vFiction. 
     *      650  1$aMagic$vFiction.
     *      
     * </pre>
     * 
     * @return String - a string representation of this record
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LEADER ");
        sb.append(getLeader().toString());
        sb.append('\n');
        Iterator<VariableField> i = getVariableFields().iterator();
        while (i.hasNext()) {
            VariableField field = (VariableField) i.next();
            sb.append(field.toString());
            sb.append('\n');
        }
        return sb.toString();
    }

    public List<VariableField> find(String pattern) {
        List<VariableField> result = new ArrayList<VariableField>();
        Iterator<ControlField> icf = controlFields.iterator();
        while (icf.hasNext()) {
            VariableField field = (VariableField) icf.next();
            if (field.find(pattern))
                result.add(field);
        }
        Iterator<DataField> idf = dataFields.iterator();
        while (idf.hasNext()) {
            VariableField field = (VariableField) idf.next();
            if (field.find(pattern))
                result.add(field);
        }
        return result;
    }

    public List<VariableField> find(String tag, String pattern) {
        List<VariableField> result = new ArrayList<VariableField>();
        Iterator<VariableField> i = getVariableFields(tag).iterator();
        while (i.hasNext()) {
            VariableField field = (VariableField) i.next();
            if (field.find(pattern))
                result.add(field);
        }
        return result;
    }

    public List<VariableField> find(String[] tag, String pattern) {
        List<VariableField> result = new ArrayList<VariableField>();
        Iterator<VariableField> i = getVariableFields(tag).iterator();
        while (i.hasNext()) {
            VariableField field = (VariableField) i.next();
            if (field.find(pattern))
                result.add(field);
        }
        return result;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

}