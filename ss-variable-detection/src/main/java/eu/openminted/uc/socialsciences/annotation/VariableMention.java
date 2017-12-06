

/* First created by JCasGen Thu Nov 30 11:52:53 CET 2017 */
package eu.openminted.uc.socialsciences.annotation;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Thu Nov 30 11:52:53 CET 2017
 * XML source: /home/local/UKP/kiaeeha/git/uc-tdm-socialsciences/ss-variable-detection/target/jcasgen/typesystem.xml
 * @generated */
public class VariableMention extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(VariableMention.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected VariableMention() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public VariableMention(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public VariableMention(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public VariableMention(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: correct

  /** getter for correct - gets 
   * @generated
   * @return value of the feature 
   */
  public String getCorrect() {
    if (VariableMention_Type.featOkTst && ((VariableMention_Type)jcasType).casFeat_correct == null)
      jcasType.jcas.throwFeatMissing("correct", "eu.openminted.uc.socialsciences.annotation.VariableMention");
    return jcasType.ll_cas.ll_getStringValue(addr, ((VariableMention_Type)jcasType).casFeatCode_correct);}
    
  /** setter for correct - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setCorrect(String v) {
    if (VariableMention_Type.featOkTst && ((VariableMention_Type)jcasType).casFeat_correct == null)
      jcasType.jcas.throwFeatMissing("correct", "eu.openminted.uc.socialsciences.annotation.VariableMention");
    jcasType.ll_cas.ll_setStringValue(addr, ((VariableMention_Type)jcasType).casFeatCode_correct, v);}    
   
    
  //*--------------*
  //* Feature: variableId

  /** getter for variableId - gets 
   * @generated
   * @return value of the feature 
   */
  public String getVariableId() {
    if (VariableMention_Type.featOkTst && ((VariableMention_Type)jcasType).casFeat_variableId == null)
      jcasType.jcas.throwFeatMissing("variableId", "eu.openminted.uc.socialsciences.annotation.VariableMention");
    return jcasType.ll_cas.ll_getStringValue(addr, ((VariableMention_Type)jcasType).casFeatCode_variableId);}
    
  /** setter for variableId - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setVariableId(String v) {
    if (VariableMention_Type.featOkTst && ((VariableMention_Type)jcasType).casFeat_variableId == null)
      jcasType.jcas.throwFeatMissing("variableId", "eu.openminted.uc.socialsciences.annotation.VariableMention");
    jcasType.ll_cas.ll_setStringValue(addr, ((VariableMention_Type)jcasType).casFeatCode_variableId, v);}    
   
    
  //*--------------*
  //* Feature: label

  /** getter for label - gets 
   * @generated
   * @return value of the feature 
   */
  public String getLabel() {
    if (VariableMention_Type.featOkTst && ((VariableMention_Type)jcasType).casFeat_label == null)
      jcasType.jcas.throwFeatMissing("label", "eu.openminted.uc.socialsciences.annotation.VariableMention");
    return jcasType.ll_cas.ll_getStringValue(addr, ((VariableMention_Type)jcasType).casFeatCode_label);}
    
  /** setter for label - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setLabel(String v) {
    if (VariableMention_Type.featOkTst && ((VariableMention_Type)jcasType).casFeat_label == null)
      jcasType.jcas.throwFeatMissing("label", "eu.openminted.uc.socialsciences.annotation.VariableMention");
    jcasType.ll_cas.ll_setStringValue(addr, ((VariableMention_Type)jcasType).casFeatCode_label, v);}    
   
    
  //*--------------*
  //* Feature: question

  /** getter for question - gets 
   * @generated
   * @return value of the feature 
   */
  public String getQuestion() {
    if (VariableMention_Type.featOkTst && ((VariableMention_Type)jcasType).casFeat_question == null)
      jcasType.jcas.throwFeatMissing("question", "eu.openminted.uc.socialsciences.annotation.VariableMention");
    return jcasType.ll_cas.ll_getStringValue(addr, ((VariableMention_Type)jcasType).casFeatCode_question);}
    
  /** setter for question - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setQuestion(String v) {
    if (VariableMention_Type.featOkTst && ((VariableMention_Type)jcasType).casFeat_question == null)
      jcasType.jcas.throwFeatMissing("question", "eu.openminted.uc.socialsciences.annotation.VariableMention");
    jcasType.ll_cas.ll_setStringValue(addr, ((VariableMention_Type)jcasType).casFeatCode_question, v);}    
   
    
  //*--------------*
  //* Feature: subQuestion

  /** getter for subQuestion - gets 
   * @generated
   * @return value of the feature 
   */
  public String getSubQuestion() {
    if (VariableMention_Type.featOkTst && ((VariableMention_Type)jcasType).casFeat_subQuestion == null)
      jcasType.jcas.throwFeatMissing("subQuestion", "eu.openminted.uc.socialsciences.annotation.VariableMention");
    return jcasType.ll_cas.ll_getStringValue(addr, ((VariableMention_Type)jcasType).casFeatCode_subQuestion);}
    
  /** setter for subQuestion - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSubQuestion(String v) {
    if (VariableMention_Type.featOkTst && ((VariableMention_Type)jcasType).casFeat_subQuestion == null)
      jcasType.jcas.throwFeatMissing("subQuestion", "eu.openminted.uc.socialsciences.annotation.VariableMention");
    jcasType.ll_cas.ll_setStringValue(addr, ((VariableMention_Type)jcasType).casFeatCode_subQuestion, v);}    
   
    
  //*--------------*
  //* Feature: answers

  /** getter for answers - gets 
   * @generated
   * @return value of the feature 
   */
  public String getAnswers() {
    if (VariableMention_Type.featOkTst && ((VariableMention_Type)jcasType).casFeat_answers == null)
      jcasType.jcas.throwFeatMissing("answers", "eu.openminted.uc.socialsciences.annotation.VariableMention");
    return jcasType.ll_cas.ll_getStringValue(addr, ((VariableMention_Type)jcasType).casFeatCode_answers);}
    
  /** setter for answers - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setAnswers(String v) {
    if (VariableMention_Type.featOkTst && ((VariableMention_Type)jcasType).casFeat_answers == null)
      jcasType.jcas.throwFeatMissing("answers", "eu.openminted.uc.socialsciences.annotation.VariableMention");
    jcasType.ll_cas.ll_setStringValue(addr, ((VariableMention_Type)jcasType).casFeatCode_answers, v);}    
  }

    