package com.konallc.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

public class RDFObfuscator {

	public static void main(String[] args) {
		
		if (args.length < 1) { 
			System.err.println("Not enough arguments received.\n"+"usage: RDFObfuscator InputFile OutputFile Fileformat. example: java -jar RDFObfuscator FileName.ttl FileNameObfuscated.ttl TTL"); 
			System.exit(0); 			
		}
		
			
		Model m = RDFDataMgr.loadModel(args[0]);
		Model mObfuscated = ModelFactory.createDefaultModel();
		boolean obfuscateFull = false;

		Iterator<Statement> iter = m.listStatements();
		while (iter.hasNext()) {			
			Statement stmt = iter.next();
			
			Resource subject = stmt.getSubject();
			Property predicate = stmt.getPredicate();
			RDFNode node = stmt.getObject();
			
			RDFNode nodenew;
			Resource newsubject;
			Property newproperty;
			
			if(obfuscateFull || !((subject.getNameSpace().equals(RDF.getURI())) || (subject.getNameSpace().equals(OWL.getURI())))) {
				newsubject = mObfuscated.createResource(obfuscate(subject.toString())); 
			}else {
				newsubject = subject;
			}
			if(obfuscateFull || !((predicate.getNameSpace().equals(RDF.getURI())) || (predicate.getNameSpace().equals(OWL.getURI())))  ) {
				newproperty = mObfuscated.createProperty(obfuscate(predicate.toString()));
			} else {
				newproperty = predicate;
			}
			
			if(node.isLiteral()) {nodenew = mObfuscated.createLiteral(obfuscate(node.asLiteral().toString()));}
			else{
				if(obfuscateFull || !( ((Resource)node).getNameSpace().equals(RDF.getURI())) || (((Resource)node).getNameSpace().equals(OWL.getURI())) ) {
					nodenew = mObfuscated.createResource(obfuscate(node.toString())); 
				} else {
					nodenew = stmt.getObject();
				}
			Statement  stmtnew = mObfuscated.createStatement(newsubject,newproperty,nodenew ) ;
			
			mObfuscated.add(stmtnew );
		}
		
		if (args.length < 2) {mObfuscated.write(System.out,"TTL");}
		else {
			try {
				String fileFormat = "TTL";
				if(args.length>2) fileFormat =args[2];
				mObfuscated.write(new FileOutputStream(args[1]),fileFormat);
			}catch(Exception ex) {
				System.out.println(ex);
			}
		}
	}
  }
	
	public static String obfuscate(String strOb) {
        StringBuilder strbuilder = new StringBuilder();
		try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(strOb.getBytes());
            byte[] bytes = md.digest();
            for(int i=0; i< bytes.length ;i++)
            {
                strbuilder.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
        } 
        catch (NoSuchAlgorithmException e) 
        {
            e.printStackTrace();
        }
        return strbuilder.toString();
	}
}
