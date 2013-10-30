package edu.cmu.lti.f13.hw4.hw4_mpiergal.casconsumers;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.f13.hw4.hw4_mpiergal.VectorSpaceRetrieval;
import edu.cmu.lti.f13.hw4.hw4_mpiergal.typesystems.Document;
import edu.cmu.lti.f13.hw4.hw4_mpiergal.typesystems.Token;
import edu.cmu.lti.f13.hw4.hw4_mpiergal.utils.Utils;


public class RetrievalEvaluator extends CasConsumer_ImplBase {

	/** query id number **/
	public ArrayList<Integer> qIdList;

	/** query and text relevant values **/
	public ArrayList<Integer> relList;

	// vocabulary for all the documents
	public HashMap<String,Integer> vocab;
	
	// all the documents
	public ArrayList<Sentence> docs;
	
	// map of queries and their associated answers
	public HashMap<Integer,QIDSet> queries; 
	
	// inverse document frequency vector
	public HashMap<String,Double> idf;
		
	public void initialize() throws ResourceInitializationException {

		qIdList = new ArrayList<Integer>();

		relList = new ArrayList<Integer>();

		vocab = new HashMap<String,Integer>();
		
		docs = new ArrayList<Sentence>();
		
		queries = new HashMap<Integer,QIDSet>();
		
		idf = new HashMap<String,Double>();
		
	}

	/**
	 * 1. construct the global word dictionary 2. keep the word
	 * frequency for each sentence
	 */
	@Override
	public void processCas(CAS aCas) throws ResourceProcessException {

		JCas jcas;
		try {
			jcas =aCas.getJCas();
		} catch (CASException e) {
			throw new ResourceProcessException(e);
		}

		FSIterator it = jcas.getAnnotationIndex(Document.type).iterator();
	
		if (it.hasNext()) {
			Document doc = (Document) it.next();
			// initialize sentence s with doc values and new hashmap
			Sentence s = new Sentence();
			s.text = doc.getText();
			s.qID = doc.getQueryID();
			s.rVal = doc.getRelevanceValue();
			s.wFreqs = new HashMap<String,Integer>();

			//Make sure that your previous annotators have populated this in CAS
			FSList fsTokenList = doc.getTokenList();
			ArrayList<Token> tokenList=Utils.fromFSListToCollection(fsTokenList, Token.class);
			
			// add qID and relevance value to qIdList and relList
			qIdList.add(doc.getQueryID());
			relList.add(doc.getRelevanceValue());
			
			// update overall vocabulary with tokens
			
			for (Token t : tokenList) {
			  String word = t.getText();
			  Integer freq = t.getFrequency();
			  // add document's frequency to the word in vocab
			  if (vocab.containsKey(word)){
			    vocab.put(word,vocab.get(word) + freq);
			  } else {
			    vocab.put(word, freq);
			  }
			  // add word,frequency to the sentence
			  s.wFreqs.put(word,freq);
			}
			
			// add sentence to list of documents
			docs.add(s);

		}

	}

	/**
	 * 1. Compute Cosine Similarity and rank the retrieved sentences 2.
	 * Compute the MRR metric
	 */
	@Override
	public void collectionProcessComplete(ProcessTrace arg0)
			throws ResourceProcessException, IOException {

		super.collectionProcessComplete(arg0);

		// create the map of query IDs to queries and sets of answers
		
		for (Sentence s : docs) {
		  if (queries.containsKey(s.qID)) {
		    // set query if the sentence is a query, else add it to the answer list
		    if (s.rVal == 99){
		      queries.get(s.qID).query = s;
		    } else {
		      queries.get(s.qID).answers.add(s);
		    }
		  } else {
		    // set up new QIDSet
		    QIDSet quid = new QIDSet();
		    quid.qID = s.qID;
		    quid.answers = new ArrayList<Sentence>();
		    // add the sentence as a query or to the answer list, whichever applies
		    if (s.rVal == 99){
          quid.query = s;
        } else {
          quid.answers.add(s);
        }
		    // put it in the queries hashmap
		    queries.put(s.qID, quid);
		  }
		}
		
		// calculate the IDF values for words in vocab
		
		int numDocs = docs.size();
		
		for (String word : vocab.keySet()) {
		  // get num of docs word occurs in
		  int k = 0;
		  for (Sentence s : docs){
		    if (s.wFreqs.containsKey(word)){
		      k++;
		    }
		  }
		  // calculate idf and put into idf vector
		  double idfVal = Math.log((double) numDocs / (double) k);
		  idf.put(word,idfVal);
		}
		
		// compute the cosine similarity measure
		for (QIDSet quid : queries.values()){
		  for (Sentence s : quid.answers){
		    s.cosSim = computeCosineSimilarity(quid.query.wFreqs,s.wFreqs);
		  }
		}
		
		// compute the rank of retrieved sentences
		
		
		
		// TODO :: compute the metric:: mean reciprocal rank
		double metric_mrr = compute_mrr();
		System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
	}

	/**
	 * 
	 * @return cosine_similarity
	 */
	private double computeCosineSimilarity(Map<String, Integer> queryVector,
			Map<String, Integer> docVector) {
		double cosine_similarity=0.0;

		// TODO :: compute cosine similarity between two sentences
		

		return cosine_similarity;
	}

	/**
	 * 
	 * @return mrr
	 */
	private double compute_mrr() {
		double metric_mrr=0.0;

		// TODO :: compute Mean Reciprocal Rank (MRR) of the text collection
		
		return metric_mrr;
	}
	
	private class Sentence {
	  public String text;
	  public int qID;
	  public int rVal;
	  // word frequency vector for the sentence
	  public HashMap<String,Integer> wFreqs;
	  // cosine similarity to query
	  public double cosSim;
	}
	
	private class QIDSet {
	  public int qID;
	  // the query sentence
	  public Sentence query;
	  // the set of answer sentences for this query ID
	  public ArrayList<Sentence> answers;
	}

}
