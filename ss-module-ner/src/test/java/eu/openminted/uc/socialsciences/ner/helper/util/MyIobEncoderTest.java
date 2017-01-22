package eu.openminted.uc.socialsciences.ner.helper.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import webanno.custom.NamedEntity;

public class MyIobEncoderTest {

	@Test
	public void testMyIobEncoderWithComplexAnnotations() throws Exception {
		String[] expected = new String[] {
				"O", "O", "O", "B-ORGgov", "I-ORGgov", "I-ORGgov", "I-ORGgov", "O", "O", "O", "O", "O", "B-ORGgov",
				"I-ORGgov", "O", "O", "O", "O"
		};

		JCas jcas = getComplexJCas();
		Type neType = JCasUtil.getType(jcas, NamedEntity.class);
		Feature neValue = neType.getFeatureByBaseName("value");
		Feature neModifier = neType.getFeatureByBaseName("modifier");

		MyIobEncoder encoder = new MyIobEncoder(jcas.getCas(), neType, neValue, neModifier, true);

		int i = 0;
		for (Token token : JCasUtil.select(jcas, Token.class)) {
			assertEquals(expected[i], encoder.encode(token));
			i++;
		}
	}

    @Test
    public void testMyIobEncoder()
            throws Exception
    {
        String[] expected = new String[] {
                "O","O","O","B-ORGgov","I-ORGgov","I-ORGgov","I-ORGgov","O","O","O","O","O","B-PERind","O","B-LOC"
                ,"O","O","O"
        };

        JCas jcas = getSimpleJCas();
        Type neType = JCasUtil.getType(jcas, NamedEntity.class);
        Feature neValue = neType.getFeatureByBaseName("value");
        Feature neModifier = neType.getFeatureByBaseName("modifier");

        MyIobEncoder encoder = new MyIobEncoder(jcas.getCas(), neType, neValue, neModifier, true);

        int i=0;
        for (Token token : JCasUtil.select(jcas, Token.class)) {
            assertEquals(expected[i], encoder.encode(token));
            i++;
        }
    }

    @Test
    public void testMyIobEncoderWithoutSubtypes()
            throws Exception
    {
        String[] expected = new String[] {
                "O","O","O","B-ORG","I-ORG","I-ORG","I-ORG","O","O","O","O","O","B-PER","O","B-LOC","O","O","O"
        };

        JCas jcas = getSimpleJCas();
        Type neType = JCasUtil.getType(jcas, NamedEntity.class);
        Feature neValue = neType.getFeatureByBaseName("value");
        Feature neModifier = neType.getFeatureByBaseName("modifier");

        MyIobEncoder encoder = new MyIobEncoder(jcas.getCas(), neType, neValue, neModifier, false);

        int i=0;
        for (Token token : JCasUtil.select(jcas, Token.class)) {
            assertEquals(expected[i], encoder.encode(token));
            i++;
        }
    }

    private JCas getSimpleJCas()
            throws Exception
    {
        String text = "We need a very complicated example sentence , which " +
                "contains as many constituents and dependencies as possible .";

        JCas jcas = JCasFactory.createJCas();
        JCasBuilder cb = new JCasBuilder(jcas);
        for (String token : text.split(" ")) {
            cb.add(token, Token.class);
        }

        List<Token> tokens = new ArrayList<>(JCasUtil.select(jcas, Token.class));
        NamedEntity ne1 = new NamedEntity(jcas, tokens.get(3).getBegin(), tokens.get(6).getEnd());
        ne1.setValue("ORG");
        ne1.setModifier("gov");
        ne1.addToIndexes();

        NamedEntity ne2 = new NamedEntity(jcas, tokens.get(12).getBegin(), tokens.get(12).getEnd());
        ne2.setValue("PER");
        ne2.setModifier("ind");
        ne2.addToIndexes();

        NamedEntity ne3 = new NamedEntity(jcas, tokens.get(14).getBegin(), tokens.get(14).getEnd());
        ne3.setValue("LOC");
        ne3.setModifier("");
        ne3.addToIndexes();

        return cb.getJCas();
    }

	private JCas getComplexJCas()
			throws Exception {
		String text = "We need a very complicated example sentence , which " +
				"contains as many constituents and dependencies as possible .";

		JCas jcas = JCasFactory.createJCas();
		JCasBuilder cb = new JCasBuilder(jcas);
		for (String token : text.split(" ")) {
			cb.add(token, Token.class);
		}

		List<Token> tokens = new ArrayList<>(JCasUtil.select(jcas, Token.class));
		NamedEntity ne1 = new NamedEntity(jcas, tokens.get(3).getBegin(), tokens.get(6).getEnd());
		ne1.setValue("ORG");
		ne1.setModifier("gov");
		ne1.addToIndexes();

		/*
		 * is contained in ne1
		 */
		NamedEntity ne2 = new NamedEntity(jcas, tokens.get(3).getBegin(), tokens.get(3).getEnd());
		ne2.setValue("PER");
		ne2.setModifier("ind");
		ne2.addToIndexes();

		NamedEntity ne3 = new NamedEntity(jcas, tokens.get(13).getBegin(), tokens.get(14).getEnd());
		ne3.setValue("LOC");
		ne3.setModifier("");
		ne3.addToIndexes();

		/*
		 * overlaps with ne3
		 */
		NamedEntity ne4 = new NamedEntity(jcas, tokens.get(12).getBegin(), tokens.get(13).getEnd());
		ne4.setValue("ORG");
		ne4.setModifier("gov");
		ne4.addToIndexes();

		return cb.getJCas();
	}
}