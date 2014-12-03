/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    CSVLoader.java
 *    Copyright (C) 2000 University of Waikato, Hamilton, New Zealand
 *
 */

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.converters.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 <!-- globalinfo-start -->
 * Reads a source that is in comma separated or tab separated format. Assumes that the first row in the file determines the number of and names of the attributes.
 * <p/>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <p/>
 * 
 * <pre> -N &lt;range&gt;
 *  The range of attributes to force type to be NOMINAL.
 *  'first' and 'last' are accepted as well.
 *  Examples: "first-last", "1,4,5-27,50-last"
 *  (default: -none-)</pre>
 * 
 * <pre> -S &lt;range&gt;
 *  The range of attribute to force type to be STRING.
 *  'first' and 'last' are accepted as well.
 *  Examples: "first-last", "1,4,5-27,50-last"
 *  (default: -none-)</pre>
 * 
 * <pre> -M &lt;str&gt;
 *  The string representing a missing value.
 *  (default: ?)</pre>
 * 
 <!-- options-end -->
 *
 * @author Mark Hall (mhall@cs.waikato.ac.nz)
 * @version $Revision: 1.1 $
 * @see Loader
 */
public class ArrayLoader 
  implements BatchConverter, OptionHandler {

  /** for serialization. */
  static final long serialVersionUID = 5607529739745491340L;
  
  /**
   * A list of hash tables for accumulating nominal values during parsing.
   */
  protected FastVector m_cumulativeStructure;

  protected Instances m_structure; 
  /**
   * Holds instances accumulated so far.
   */
  protected FastVector m_cumulativeInstances;
  
  /** Tokenizer for the data. */
  protected transient String[][] m_data;
  
  protected transient String[] m_column;
  
  /** The range of attributes to force to type nominal. */
  protected Range m_NominalAttributes = new Range();
  
  /** The range of attributes to force to type string. */
  protected Range m_StringAttributes = new Range();
  
  /** The placeholder for missing values. */
  protected String m_MissingValue = "";
  
  /** whether the first row has been read. */
  protected boolean m_FirstCheck;
  
  /**
   * default constructor.
   */
  public ArrayLoader() { }

  /**
   * Returns a string describing this attribute evaluator.
   * 
   * @return a description of the evaluator suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {
    return "Reads a source that is in comma separated or tab separated format. "
      +"Assumes that the first row in the file determines the number of "
      +"and names of the attributes.";
  }

  /**
   * Returns an enumeration describing the available options.
   *
   * @return an enumeration of all the available options.
   */
  public Enumeration listOptions() {
    Vector result = new Vector();
    
    result.addElement(new Option(
        "\tThe range of attributes to force type to be NOMINAL.\n"
        + "\t'first' and 'last' are accepted as well.\n"
        + "\tExamples: \"first-last\", \"1,4,5-27,50-last\"\n"
        + "\t(default: -none-)",
        "N", 1, "-N <range>"));
    
    result.addElement(new Option(
        "\tThe range of attribute to force type to be STRING.\n"
        + "\t'first' and 'last' are accepted as well.\n"
        + "\tExamples: \"first-last\", \"1,4,5-27,50-last\"\n"
        + "\t(default: -none-)",
        "S", 1, "-S <range>"));
    
    result.addElement(new Option(
        "\tThe string representing a missing value.\n"
        + "\t(default: ?)",
        "M", 1, "-M <str>"));
      
    return result.elements();
  }

  /**
   * Parses a given list of options. <p/>
   *
   <!-- options-start -->
   * Valid options are: <p/>
   * 
   * <pre> -N &lt;range&gt;
   *  The range of attributes to force type to be NOMINAL.
   *  'first' and 'last' are accepted as well.
   *  Examples: "first-last", "1,4,5-27,50-last"
   *  (default: -none-)</pre>
   * 
   * <pre> -S &lt;range&gt;
   *  The range of attribute to force type to be STRING.
   *  'first' and 'last' are accepted as well.
   *  Examples: "first-last", "1,4,5-27,50-last"
   *  (default: -none-)</pre>
   * 
   * <pre> -M &lt;str&gt;
   *  The string representing a missing value.
   *  (default: ?)</pre>
   * 
   <!-- options-end -->
   *
   * @param options the list of options as an array of strings
   * @throws Exception if an option is not supported
   */
  public void setOptions(String[] options) throws Exception {
    String	tmpStr;

    tmpStr = Utils.getOption('N', options);
    if (tmpStr.length() != 0)
      setNominalAttributes(tmpStr);
    else
      setNominalAttributes("");

    tmpStr = Utils.getOption('S', options);
    if (tmpStr.length() != 0)
      setStringAttributes(tmpStr);
    else
      setStringAttributes("");

    tmpStr = Utils.getOption('M', options);
    if (tmpStr.length() != 0)
      setMissingValue(tmpStr);
    else
      setMissingValue("?");
  }

  /**
   * Gets the current settings of the Classifier.
   *
   * @return an array of strings suitable for passing to setOptions
   */
  public String[] getOptions() {
    Vector<String>	result;
    
    result  = new Vector<String>();

    if (getNominalAttributes().length() > 0) {
      result.add("-N");
      result.add(getNominalAttributes());
    }
    
    if (getStringAttributes().length() > 0) {
      result.add("-S");
      result.add(getStringAttributes());
    }

    result.add("-M");
    result.add(getMissingValue());
    
    return result.toArray(new String[result.size()]);
  }
  
  /**
   * Sets the attribute range to be forced to type nominal.
   * 
   * @param value	the range
   */
  public void setNominalAttributes(String value) {
    m_NominalAttributes.setRanges(value);
  }
  
  /**
   * Returns the current attribute range to be forced to type nominal.
   * 
   * @return		the range
   */
  public String getNominalAttributes() {
    return m_NominalAttributes.getRanges();
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   *         		displaying in the explorer/experimenter gui
   */
  public String nominalAttributesTipText() {
    return 
        "The range of attributes to force to be of type NOMINAL, example "
      + "ranges: 'first-last', '1,4,7-14,50-last'.";
  }
  
  /**
   * Sets the attribute range to be forced to type string.
   * 
   * @param value	the range
   */
  public void setStringAttributes(String value) {
    m_StringAttributes.setRanges(value);
  }
  
  /**
   * Returns the current attribute range to be forced to type string.
   * 
   * @return		the range
   */
  public String getStringAttributes() {
    return m_StringAttributes.getRanges();
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   *         		displaying in the explorer/experimenter gui
   */
  public String stringAttributesTipText() {
    return 
        "The range of attributes to force to be of type STRING, example "
      + "ranges: 'first-last', '1,4,7-14,50-last'.";
  }
  
  /**
   * Sets the placeholder for missing values.
   * 
   * @param value	the placeholder
   */
  public void setMissingValue(String value) {
    m_MissingValue = value;
  }
  
  /**
   * Returns the current placeholder for missing values.
   * 
   * @return		the placeholder
   */
  public String getMissingValue() {
    return m_MissingValue;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   *         		displaying in the explorer/experimenter gui
   */
  public String missingValueTipText() {
    return "The placeholder for missing values, default is '?'.";
  }
  
  /**
   * Resets the Loader object and sets the source of the data set to be 
   * the supplied Stream object.
   *
   * @param input the input stream
   * @exception IOException if an error occurs
   */
  public void setSource(String[][] data, String[] column) throws IOException {    
    m_structure    = null;
    m_FirstCheck   = true;
    m_data         = data;
    m_column       = column;
  }

  /**
   * Determines and returns (if possible) the structure (internally the 
   * header) of the data set as an empty set of instances.
   *
   * @return the structure of the data set as an empty set of Instances
   * @exception IOException if an error occurs
   */
  public Instances getStructure() throws IOException {
    if (m_data == null) {
      throw new IOException("No source has been specified");
    }

    if (m_structure == null) {
      try {
          readStructure(m_column);
      } catch (FileNotFoundException ex) {
      }
    }
    
    return m_structure;
  }

  /**
   * reads the structure.
   * 
   * @param st the stream tokenizer to read from
   * @throws IOException if reading fails
   */
  private void readStructure(String[] columns) throws IOException {
    readHeader(columns);
  }

  /**
   * Return the full data set. If the structure hasn't yet been determined
   * by a call to getStructure then method should do so before processing
   * the rest of the data set.
   *
   * @return the structure of the data set as an empty set of Instances
   * @exception IOException if there is no source or parsing fails
   */
  public Instances getDataSet() throws IOException {
    if (m_data == null) {
      throw new IOException("No source has been specified");
    }

    if (m_structure == null) {
      getStructure();
    }

    m_cumulativeStructure = new FastVector(m_structure.numAttributes());
    for (int i = 0; i < m_structure.numAttributes(); i++) {
      m_cumulativeStructure.addElement(new Hashtable());
    }

    m_cumulativeInstances = new FastVector();
    FastVector current;

    for (int i = 0; i < m_data.length; i++)
    {
       current = getInstance(m_data[i]);

       m_cumulativeInstances.addElement(current);
    }

    FastVector atts = new FastVector(m_structure.numAttributes());
    for (int i = 0; i < m_structure.numAttributes(); i++) {
      String attname = m_structure.attribute(i).name();
      Hashtable tempHash = ((Hashtable)m_cumulativeStructure.elementAt(i));
      if (tempHash.size() == 0) {
	atts.addElement(new Attribute(attname));
      } else {
	if (m_StringAttributes.isInRange(i)) {
	  atts.addElement(new Attribute(attname, (FastVector) null));
	}
	else {
	  FastVector values = new FastVector(tempHash.size());
	  // add dummy objects in order to make the FastVector's size == capacity
	  for (int z = 0; z < tempHash.size(); z++) {
	    values.addElement("dummy");
	  }
	  Enumeration e = tempHash.keys();
	  while (e.hasMoreElements()) {
	    Object ob = e.nextElement();
	    //	  if (ob instanceof Double) {
	    int index = ((Integer)tempHash.get(ob)).intValue();
	    String s = ob.toString();
	    if (s.startsWith("'") || s.startsWith("\""))
	      s = s.substring(1, s.length() - 1);
	    values.setElementAt(new String(s), index);
	    //	  }
	  }
	  atts.addElement(new Attribute(attname, values));
	}
      }
    }

    // make the instances
    String relationName;
    relationName = "ArrayData";
    Instances dataSet = new Instances(relationName, 
				      atts, 
				      m_cumulativeInstances.size());

    for (int i = 0; i < m_cumulativeInstances.size(); i++) {
      current = ((FastVector)m_cumulativeInstances.elementAt(i));
      double [] vals = new double[dataSet.numAttributes()];
      for (int j = 0; j < current.size(); j++) {
	Object cval = current.elementAt(j);
	if (cval instanceof String) {
	  if (((String)cval).compareTo(m_MissingValue) == 0) {
	    vals[j] = Instance.missingValue();
	  } else {
	    if (dataSet.attribute(j).isString()) {
	      vals[j] = dataSet.attribute(j).addStringValue((String) cval);
	    }
	    else if (dataSet.attribute(j).isNominal()) {
	      // find correct index
	      Hashtable lookup = (Hashtable)m_cumulativeStructure.elementAt(j);
	      int index = ((Integer)lookup.get(cval)).intValue();
	      vals[j] = index;
	    }
	    else {
	      throw new IllegalStateException("Wrong attribute type at position " + (i+1) + "!!!");
	    }
	  }
	} else if (dataSet.attribute(j).isNominal()) {
	  // find correct index
	  Hashtable lookup = (Hashtable)m_cumulativeStructure.elementAt(j);
	  int index = ((Integer)lookup.get(cval)).intValue();
	  vals[j] = index;
	} else if (dataSet.attribute(j).isString()) {
	  vals[j] = dataSet.attribute(j).addStringValue("" + cval);
	} else {
	  vals[j] = ((Double)cval).doubleValue();
	}
      }
      dataSet.add(new Instance(1.0, vals));
    }
    m_structure = new Instances(dataSet, 0);
    m_cumulativeStructure = null; // conserve memory

    return dataSet;
  }

  /**
   * Attempts to parse a line of the data set.
   *
   * @param tokenizer the tokenizer
   * @return a FastVector containg String and Double objects representing
   * the values of the instance.
   * @exception IOException if an error occurs
   *
   * <pre><jml>
   *    private_normal_behavior
   *      requires: tokenizer != null;
   *      ensures: \result  != null;
   *  also
   *    private_exceptional_behavior
   *      requires: tokenizer == null
   *                || (* unsucessful parse *);
   *      signals: (IOException);
   * </jml></pre>
   */
  private FastVector getInstance(String[] data) 
    throws IOException {

    FastVector current = new FastVector();
    
    for (int i = 0; i < data.length; i++)
    {
        if (data[i].equals(m_MissingValue))
        {
            current.addElement(new String(m_MissingValue));
        }
        else
        {
            // try to parse as a number
            try
            {
                double val = Double.valueOf(data[i]).doubleValue();
                current.addElement(new Double(val));
            } catch (NumberFormatException e) {
                // otherwise assume its an enumerated value
                current.addElement(new String(data[i]));
            }
        }
    }

    // check number of values read
    if (current.size() != m_structure.numAttributes()) {
        System.out.println("Incorrect Structure");
    }

    // check for structure update
    try {
      checkStructure(current);
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return current;
  }

  /**
   * Checks the current instance against what is known about the structure
   * of the data set so far. If there is a nominal value for an attribute
   * that was beleived to be numeric then all previously seen values for this
   * attribute are stored in a Hashtable.
   *
   * @param current a <code>FastVector</code> value
   * @exception Exception if an error occurs
   *
   * <pre><jml>
   *    private_normal_behavior
   *      requires: current != null;
   *  also
   *    private_exceptional_behavior
   *      requires: current == null
   *                || (* unrecognized object type in current *);
   *      signals: (Exception);
   * </jml></pre>
   */
  private void checkStructure(FastVector current) throws Exception {
    if (current == null) {
      throw new Exception("current shouldn't be null in checkStructure");
    }

    // initialize ranges, if necessary
    if (m_FirstCheck) {
      m_NominalAttributes.setUpper(current.size() - 1);
      m_StringAttributes.setUpper(current.size() - 1);
      m_FirstCheck = false;
    }
    
    for (int i = 0; i < current.size(); i++) {
      Object ob = current.elementAt(i);
      if ((ob instanceof String) || (m_NominalAttributes.isInRange(i)) || (m_StringAttributes.isInRange(i))) {
	if (ob.toString().compareTo(m_MissingValue) == 0) {
	  // do nothing
	} else {
	  Hashtable tempHash = (Hashtable)m_cumulativeStructure.elementAt(i);
	  if (!tempHash.containsKey(ob)) {
	    // may have found a nominal value in what was previously thought to
	    // be a numeric variable.
	    if (tempHash.size() == 0) {
	      for (int j = 0; j < m_cumulativeInstances.size(); j++) {
		FastVector tempUpdate = 
		  ((FastVector)m_cumulativeInstances.elementAt(j));
		Object tempO = tempUpdate.elementAt(i);
		if (tempO instanceof String) {
		  // must have been a missing value
		} else {
		  if (!tempHash.containsKey(tempO)) {
		    tempHash.put(new Double(((Double)tempO).doubleValue()), 
				 new Integer(tempHash.size()));
		  }
		}
	      }
	    }
	    int newIndex = tempHash.size();
	    tempHash.put(ob, new Integer(newIndex));
	  }
	}
      } else if (ob instanceof Double) {
	Hashtable tempHash = (Hashtable)m_cumulativeStructure.elementAt(i);
	if (tempHash.size() != 0) {
	  if (!tempHash.containsKey(ob)) {
	    int newIndex = tempHash.size();
	    tempHash.put(new Double(((Double)ob).doubleValue()), 
				    new Integer(newIndex));
	  }
	}
      } else {
	throw new Exception("Wrong object type in checkStructure!");
      }
    }
  }

  /**
   * Assumes the first line of the file contains the attribute names.
   * Assumes all attributes are real (Reading the full data set with
   * getDataSet will establish the true structure).
   *
   */
  private void readHeader(String[] column) throws IOException {
   
    FastVector attribNames = new FastVector();

    // Assume first row of data are the column titles
    for (int i = 0; i < column.length; i++)
    {
        attribNames.addElement(new Attribute(column[i]));
    }

    m_structure = new Instances("DataArray", attribNames, 0);
  }
  
  /**
   * Resets the Loader ready to read a new data set or the
   * same data set again.
   * 
   * @throws IOException if something goes wrong
   */
  public void reset() throws IOException {
    m_structure = null;
    
  }
  
  /**
   * Returns the revision string.
   * 
   * @return		the revision
   */
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 1.1 $");
  }

  /**
   * Main method.
   *
   * @param args should contain the name of an input file.
   */
  public static void main(String [] args) {
  }
}
