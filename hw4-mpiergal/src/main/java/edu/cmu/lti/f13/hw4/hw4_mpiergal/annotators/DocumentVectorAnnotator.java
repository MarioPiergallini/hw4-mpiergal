/** DocumentVectorAnnotator.java
 * @author Mario Piergallini
 */

package edu.cmu.lti.f13.hw4.hw4_mpiergal.annotators;

import java.util.HashSet;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
//import org.apache.uima.jcas.cas.IntegerArray;
//import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.lti.f13.hw4.hw4_mpiergal.typesystems.Document;
import edu.cmu.lti.f13.hw4.hw4_mpiergal.typesystems.Token;
import edu.cmu.lti.f13.hw4.hw4_mpiergal.utils.Utils;

public class DocumentVectorAnnotator extends JCasAnnotator_ImplBase {

  public HashSet<String> stopwords;
  
	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {

	  stopwords = new HashSet<String>(174);
	  String stopwordsText = "a about above after again against all am an and any are aren't as at be because been before being below between both but by can't cannot could couldn't did didn't do does doesn't doing don't down during each few for from further had hadn't has hasn't have haven't having he he'd he'll he's her here here's hers herself him himself his how how's i i'd i'll i'm i've if in into is isn't it it's its itself let's me more most mustn't my myself no nor not of off on once only or other ought our ours ourselves out over own same shan't she she'd she'll she's should shouldn't so some such than that that's the their theirs them themselves then there there's these they they'd they'll they're they've this those through to too under until up very was wasn't we we'd we'll we're we've were weren't what what's when when's where where's which while who who's whom why why's with won't would wouldn't you you'd you'll you're you've your yours yourself yourselves";
	  
	  for (String word : stopwordsText.split(" ")) {stopwords.add(word);}
	  
		FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
		if (iter.isValid()) {
			iter.moveToNext();
			Document doc = (Document) iter.get();
			createTermFreqVector(jcas, doc);
		}

	}
	/**
	 * 
	 * @param jcas
	 * @param doc
	 */

	private void createTermFreqVector(JCas jcas, Document doc) {

		String docText = doc.getText();
		
		HashMap<String,Token> tMap = new HashMap<String,Token>();
		
		Pattern tokenPattern = Pattern.compile("[\\w']+");
		Matcher tokenMatcher = tokenPattern.matcher(docText);

		int pos = 0;
		
		while (tokenMatcher.find(pos)){
		  //found a match, get token values
		  int start = tokenMatcher.start();
		  int end = tokenMatcher.end();
		  String word = docText.substring(start,end).toLowerCase();
		  // check if the word is a stop word
      if (stopwords.contains(word)){
        pos = tokenMatcher.end();
        continue;
      }
      Token token = new Token(jcas);
		  token.setBegin(start);
		  token.setEnd(end);
		  token.setText(word);
		  token.setFrequency(1);
		  // add token to indices
		  token.addToIndexes();
		  pos = tokenMatcher.end();
		  
		  if (tMap.containsKey(word)){
		    tMap.get(word).setFrequency(tMap.get(word).getFrequency()+1);
		  } else {
		    tMap.put(word,token);
		  }
		  
		  doc.setTokenList(Utils.fromCollectionToFSList(jcas,tMap.values()));
		}
	}

}
