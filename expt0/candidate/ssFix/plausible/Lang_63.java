package edu.ksu.cis.bnj.bbn.converter;

import java.awt.geom.*;
import java.io.*;
import java.lang.*;
import java.util.*;
import javax.swing.*;

/**
  * This class is responsible for the reading and writing of the Bayesian
  * Networks to disk.  It reads them in in the BNIF 0.13 format and then
  * will put them in the corresponding data structure so that the network
  * can be manipulated.
  *
  * Fix the bug for bif file format , and extend it to Genie(.dsl) and Hugin(.net) format on Friday, Nov. 02, 2001
  * Now it can load .xml, .bif, .dsl and .net file format; it can save to .xml and .bif format
  *
  * Updated on Wednesday, Nov. 07, 2001 --> now it can load 6 BBN formats: .bif, .xml, .xbn, .dsc, .dsl, .net
  * Updated on Thursday, Nov. 08, 2001 --> now it can load 2 more BBN formats: .dnet, .ent
  *
  * @author Haipeng Guo
  * @author Laura Kruse
  * @version v1.2
  */
public class FileIO {
	private BufferedWriter outfile;

	private LinkedList baynet;
	private int[][] goldstandard;

	private String currenttype;

	private final int width = 80;
	private final int height = 50;

	/**
	  * The constructor for the Bayesian Network class.  It will provide
	  * the functionality to load a Bayesian Network from disk or to
	  * save a newly created or modified Bayesian Network.
	  */
	public FileIO() {
		currenttype="xml";
	}

	/**
	  * This function takes in a Bayesian Network that has already been
	  * created and will then save it to disk.  It will save it to the file
	  * format that the user has specified.
	  *
	  * @param filename the file that this network is being saved to
	  * @param baynet the Bayesian Network that needs to be saved
	  */
	public void save(String filename, LinkedList baynet) {
		this.baynet = baynet;

		try {
			outfile = new BufferedWriter(new FileWriter(filename));

                        System.out.println("save to ." + currenttype + " file format ...");

			if(currenttype=="bif") {
				currenttype="bif";
				saveBIF();
			} else if(currenttype=="xml"){
				currenttype="xml";
				saveXMLBIF();
			} else {
                                currenttype="net";
                                SaveHuginNet();
                        }

			outfile.close();
                        JOptionPane.showMessageDialog(null,"Bayesian Network Saved!","File Saved",JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

          /**
	  * This function takes in a Bayesian Network that has already been
	  * created and will then save it to disk.  It will save it to the file
	  * format that the user has specified.
	  *
	  * @param filename the file that this network is being saved to
	  * @param baynet the Bayesian Network that needs to be saved
	  */
	public void convert(String outputfilename, LinkedList baynet, String outputfiletype) {
		this.baynet = baynet;

		try {
			outfile = new BufferedWriter(new FileWriter(outputfilename));
                        this.setFileType(outputfiletype);
                        System.out.println("outputfiletype ="+outputfiletype);

			if(currenttype.equals("bif")) {
				currenttype="bif";
                                System.out.println("convert to BIF .bif format!");
				saveBIF();
			} else if(currenttype.equals("xml")){
				currenttype="xml";
                                System.out.println("convert to XMLBIF .xml format!");
				saveXMLBIF();
			} else if(currenttype.equals("net")){
                                System.out.println("convert to Hugin .net format!");
                                currenttype="net";
                                SaveHuginNet();
                        } else {
                                System.out.println("Sorry, I can't do this format!");
                        }

			outfile.close();
                        //JOptionPane.showMessageDialog(null,"Bayesian Network Saved!","File Saved",JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
        /**
	  * The save function for the Hugin .net file format.  This function is
	  * responsible for calling the different parts of the save functions.
	  * It pieces all of the parts of the file togeather in the right
	  * order and lets the user know when the file has been saved
	  * successfully.
	  */
        private void SaveHuginNet(){

                try {
			saveHeaderHuginNet();
			outfile.write("\n");

			saveVariablesHuginNet();
                        outfile.write("\n");

			saveProbabilityDistributionsHuginNet();
                        outfile.write("\n");

		} catch(IOException ioe) {
			ioe.printStackTrace();
		}

        }

	/**
	  * The save function for the XMLBIF file format.  This function is
	  * responsible for calling the different parts of the save functions.
	  * It pieces all of the parts of the file togeather in the right
	  * order and lets the user know when the file has been saved
	  * successfully.
	  */
	private void saveXMLBIF() {
		try {
			saveHeaderXMLBIF();
			outfile.write("\t<NAME>bayesiannetwork</NAME>\n");

			outfile.write("\t\t<!-- Variables -->\n");
			saveVariablesXMLBIF();

			outfile.write("\t\t<!-- Probability Distributions -->\n");
			saveProbabilityDistributionsXMLBIF();

			outfile.write("\t</NETWORK>\n");
			outfile.write("</BIF>\n");

		} catch(IOException ioe) {
			ioe.printStackTrace();
		}

		//JOptionPane.showMessageDialog(null,"Bayesian Network Saved!","File Saved",JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	  * This function is responsible for saving the XML header to the file.
	  * The header never changes and it has the DTD for this particular
	  * format.
	  */
	private void saveHeaderXMLBIF() {
		try {
			outfile.write("<?xml version=\"1.0\"?>\n\n");
			outfile.write("<!-- DTD for the XMLBIF 0.3 format -->\n");
			outfile.write("<!DOCTYPE BIF [\n");
			outfile.write("\t<!ELEMENT BIF ( NETWORK )*>\n");
			outfile.write("\t\t<!ATTLIST BIF VERSION CDATA #REQUIRED>\n");
			outfile.write("\t<!ELEMENT NETWORK ( NAME, ( PROPERTY | VARIABLE | DEFINITION )* )>\n");
			outfile.write("\t<!ELEMENT NAME (#PCDATA)>\n");
			outfile.write("\t<!ELEMENT VARIABLE ( NAME, ( OUTCOME | PROPERTY )* ) >\n");
			outfile.write("\t\t<!ATTLIST VARIABLE TYPE (nature|decision|utility) \"nature\">\n");
			outfile.write("\t<!ELEMENT OUTCOME (#PCDATA)>\n");
			outfile.write("\t<!ELEMENT DEFINITION ( FOR | GIVEN | TABLE | PROPERTY )* >\n");
			outfile.write("\t<!ELEMENT FOR (#PCDATA)>\n");
			outfile.write("\t<!ELEMENT GIVEN (#PCDATA)>\n");
			outfile.write("\t<!ELEMENT TABLE (#PCDATA)>\n");
			outfile.write("\t<!ELEMENT PROPERTY (#PCDATA)>\n");
			outfile.write("]>\n\n");

			outfile.write("<BIF VERSION=\"0.3\">\n");
			outfile.write("\t<NETWORK>\n");

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	  * This function traverses the list of nodes and records the
	  * information about them.  It will record what the type of each
	  * node is, and what the name and the states of the node are.  In
	  * addition it records the location of the Bayesian Network in
	  * the graphical representation.
	  */
	private void saveVariablesXMLBIF() {
		Item current;

		for(int loc = 0; loc < baynet.size(); loc++) {
			current = (Item) baynet.get(loc);
			LinkedList states = ((ChanceBlock) current.getItem()).getAttributeNames();
			String type = current.getItem().getType();
			String name = current.getItem().getBlockName();

			double x = current.getItem().getx();
			double y = current.getItem().gety();

			try {
				outfile.write("\t\t<VARIABLE TYPE=\""+type+"\">\n");
				outfile.write("\t\t\t<NAME>"+name+"</NAME>\n");

				for(int i=0;i<states.size();i++) {
					outfile.write("\t\t\t<OUTCOME>"+states.get(i)+"</OUTCOME>\n");
				}

				outfile.write("\t\t\t<PROPERTY>position = ("+(int)x+", "+(int)y+")</PROPERTY>\n");
				outfile.write("\t\t</VARIABLE>\n");

			} catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	/**
	  * This function saves the information about a specific dependency
	  * in the table.  It records all of the associated probabilies that
	  * are generated from the different dependencies.  If a particular
	  * node has no dependencies, then it will record the probabilites
	  * for that particular state.
	  */
	private void saveProbabilityDistributionsXMLBIF() {
		Item current;

		for(int loc=0;loc<baynet.size();loc++) {
			current = (Item) baynet.get(loc);

			String name = current.getItem().getBlockName();

			int numparents = current.numParents();
			int rows = ((ChanceBlock)current.getItem()).getRows();
			int columns = ((ChanceBlock)current.getItem()).getColumns();
			try {
				outfile.write("\t\t<DEFINITION>\n");
				outfile.write("\t\t\t<FOR>"+name+"</FOR>\n");

				for(int i=0;i<numparents;i++) {
					String parent = current.getParent(i).getItem().getBlockName();
					outfile.write("\t\t\t<GIVEN>"+parent+"</GIVEN>\n");
				}

				outfile.write("\t\t\t<TABLE>");
				StringBuffer vals = new StringBuffer();

				for(int j=0;j<rows;j++) {
					for(int k=0;k<columns;k++) {
						double value = ((ChanceBlock) current.getItem()).getValue(j, k);
						vals.append(value + " ");
					}
				}

				// Old bad code
				/*
				for(int j=0;j<columns;j++) {
					for(int k=0;k<rows;k++) {
						double value = ((ChanceBlock)current.getItem()).getValue(k,j);
						vals.append(value+" ");
					}
				}
				*/

				outfile.write(vals.toString());
				outfile.write("</TABLE>\n");

				outfile.write("\t\t</DEFINITION>\n");

			} catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	/**
	  * This function is responsible for saving the Bayesian Network
	  * in the BIF format.  It calls all of the appropiate saving functions
	  * and then will let the user know when the file has been saved.
	  */
	private void saveBIF() {
		this.saveHeaderBIF();
		this.saveVariablesBIF();
		this.saveProbabilityDistributionsBIF();

		//JOptionPane.showMessageDialog(null,"Bayesian Network Saved!","File Saved",JOptionPane.INFORMATION_MESSAGE);
	}

        private void saveHeaderHuginNet() {
                try {
			outfile.write("net\n");
                        outfile.write("{\n");
                        outfile.write("node_size = (90 40);");
                        outfile.write("\n}\n");

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

        }

        private void saveVariablesHuginNet() {

                Item current;
                String sPostions="";
                String sLabel="";
                String sStates="";
                String noSpaceName="";


		for(int loc = 0; loc < baynet.size(); loc++) {
			current = (Item) baynet.get(loc);
			LinkedList states = ((ChanceBlock) current.getItem()).getAttributeNames();

			String name = current.getItem().getBlockName().trim();

                        //System.out.println("name.indexOf(space) :"+name.indexOf(" "));
                        // in .net, space is not allowed in node name
                        if(name.indexOf("?")>=0 || name.indexOf(" ")>=0 || name.indexOf("-")>=0 || name.indexOf(".")>=0) {
                           noSpaceName = name.replace('?', '_');
                           noSpaceName = noSpaceName.replace(' ', '_');
                           noSpaceName = noSpaceName.replace('-', '_');
                           noSpaceName = noSpaceName.replace('.', '_');
                           //System.out.println("noSpaceName :"+noSpaceName);
                        }
                        else noSpaceName = name;

			double x = current.getItem().getx();
			double y = current.getItem().gety();
                        y = 450 -y;

			try {
				sLabel = "\tlabel = \""+name+"\";\n";
                                sPostions ="\tposition = ("+(int)x+" "+(int)y+");\n";
                                sStates = "\tstates = (";

				for(int i=0;i<states.size();i++) {
                                        sStates = sStates.trim() + " \""+ states.get(i)+"\"";
                                }
                                sStates = sStates +");\n";
                                outfile.write("\nnode "+noSpaceName+"\n{\n");
                                outfile.write(sLabel);
                                outfile.write(sPostions);
                                outfile.write(sStates);
                                outfile.write("}\n");

			} catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}
        }

        private void saveProbabilityDistributionsHuginNet() {

		Item current;

                /*
{
 data = ((( 0.994 0.006 )	%  yes  yes
	  ( 0.96 0.04 ))	%  yes  no
	 (( 0.88 0.12 )	%  no  yes
	  ( 0.2 0.8 )));	%  no  no
}
                */

		for(int loc=0;loc<baynet.size();loc++) {
                        String sPotential="potential (";
                        String sParent="";
                        Vector tmpData;
                        String noSpaceName;
			current = (Item) baynet.get(loc);

			String name = current.getItem().getBlockName();
                        if(name.indexOf("?")>=0 || name.indexOf(" ")>=0 || name.indexOf("-")>=0 || name.indexOf(".")>=0) {
                           noSpaceName = name.replace('?', '_');
                           noSpaceName = noSpaceName.replace(' ', '_');
                           noSpaceName = noSpaceName.replace('-', '_');
                           noSpaceName = noSpaceName.replace('.', '_');
                           //System.out.println("noSpaceName :"+noSpaceName);

                        }
                        else noSpaceName = name;

                        ChanceBlock b;
                        b = (ChanceBlock)current.getItem();
                        int numOfStates = b.numAttributes();
                        //System.out.println("numOfStates = " + numOfStates);

                        Vector parentNumOfStates = new Vector();
                        int numOfParents = current.numParents();
                        for(int i=0;i<numOfParents;i++) {
                           b = (ChanceBlock)current.getParent(i).getItem();
                           parentNumOfStates.addElement(String.valueOf(b.numAttributes()));
                        }
                         //System.out.println("name = " + name);
                         //System.out.println("parentNumOfStates = " + parentNumOfStates.toString());

                         for(int i=parentNumOfStates.size()-2;i>=0;i--) {
                             int tmp1 = Integer.parseInt(parentNumOfStates.elementAt(i).toString().trim());
                             int tmp2 = Integer.parseInt(parentNumOfStates.elementAt(i+1).toString().trim());
                             tmp1 = tmp1*tmp2;
                             parentNumOfStates.setElementAt(String.valueOf(tmp1), i);
                         }

                         //System.out.println("parentNumOfStates = " + parentNumOfStates.toString());



                        String sData = " data = ";

			int numparents = current.numParents();
                        if(numparents==0) {
                              sPotential = sPotential +noSpaceName + " | ";
                        } else {
                              sPotential = sPotential +noSpaceName + " | ";
                        }
			int rows = ((ChanceBlock)current.getItem()).getRows();
			int columns = ((ChanceBlock)current.getItem()).getColumns();
			try {
				for(int i=0;i<numparents;i++) {
					String parent = current.getParent(i).getItem().getBlockName();


                                        if(parent.indexOf("?")>=0 || parent.indexOf(" ")>=0 || parent.indexOf("-")>=0 ||parent.indexOf(".")>=0) {
                                           parent = parent.replace('?', '_');
                                           parent = parent.replace(' ', '_');
                                           parent = parent.replace('-', '_');
                                           parent = parent.replace('.', '_');
                                        }
                                        sParent = sParent+" "+parent;
				}
                                sPotential = sPotential + sParent +")\n";

/*
data =  0.994 0.0060
0.96 0.04
0.88 0.12
0.2 0.8
data = (( 0.994 0.006 )	%  yes  yes
	  ( 0.96 0.04 ))	%  yes  no
	 (( 0.88 0.12 )	%  no  yes
	  ( 0.2 0.8 ))	%  no  no

*/
				StringBuffer vals = new StringBuffer();
//System.out.println("columns="+columns);
//System.out.println("rows="+rows);
int counter=0;
tmpData = new Vector();
                                for(int k=0;k<columns;k++) {
                                        vals.append("(");
                                        String oneLine="";
				        for(int j=0;j<rows;j++) {

						double value = ((ChanceBlock) current.getItem()).getValue(j, k);
						vals.append(value + " ");
                                                oneLine = oneLine + " " + String.valueOf(value);
                                                counter++;
					}
                                        tmpData.addElement("( "+oneLine+" )");
                                        vals.append(")\n\t");
				}
                                //System.out.println("vals = "+vals);
                                //System.out.println("tmpData.size "+tmpData.size());
                                //System.out.println("tmpData = "+tmpData.toString());

                                for(int i=0;i<tmpData.size();i++) {
                                   for(int j=0;j<parentNumOfStates.size();j++) {
                                       int tmp = Integer.parseInt(parentNumOfStates.elementAt(j).toString().trim());
                                       if(i==0)
                                          tmpData.setElementAt("("+tmpData.elementAt(i).toString(),i);
                                       if(((i+1)%tmp)==0) {
                                          tmpData.setElementAt(tmpData.elementAt(i).toString()+")",i);
                                          if(i!=(tmpData.size()-1)) {
                                            tmpData.setElementAt("("+tmpData.elementAt(i+1).toString(),i+1);
                                          }
                                       }
                                   }
                                }

                                tmpData.setElementAt(tmpData.lastElement()+";", tmpData.size()-1);
                                //System.out.println("new tmpData...");
                                //for(int i=0;i<tmpData.size();i++) {
                                //    System.out.println(tmpData.elementAt(i).toString());
                                //}

                                outfile.write(sPotential+"{\n");
                                outfile.write(sData);
                                for(int i=0;i<tmpData.size();i++) {
                                    outfile.write("\t"+tmpData.elementAt(i).toString()+"\n");
                                }
                                outfile.write("}\n");
                                outfile.write("\n");



			} catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}

        }

	/**
	  * This function is responsible for saving the header for the bif
	  * file format.
	  */
	private void saveHeaderBIF() {
		try {
			outfile.write("// Bayesian network\n");
			//outfile.write("network \"BayesianNetwork\" { // "+baynet.getItemCount()+" variables and "+baynet.getItemCount()+" probability distributions\n");
			outfile.write("network \"BayesianNetwork\" { // " + baynet.size() + " variables and " + baynet.size() + " probability distributions\n");
			outfile.write("}\n");

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	  * This function traverses the list of nodes and records the
	  * information about them.  It will record what the type of each
	  * node is, and what the name and the states of the node are.  In
	  * addition it records the location of the Bayesian Network in
	  * the graphical representation.
	  */
	private void saveVariablesBIF() {
		Item current;
		for(int loc=0;loc<baynet.size();loc++) {
			current = (Item) baynet.get(loc);

			LinkedList states = ((ChanceBlock) current.getItem()).getAttributeNames();
			String type = current.getItem().getType();
			String name = current.getItem().getBlockName();

			double x = current.getItem().getx();
			double y = current.getItem().gety();

			try {
				outfile.write("variable  \""+name+"\" { // "+states.size()+" values\n");
				outfile.write("\ttype discrete["+states.size()+"] {");
				for(int i=0;i<states.size();i++) {
					outfile.write("  \""+states.get(i)+"\"");
				}
				outfile.write(" };\n");
				outfile.write("\tproperty \"position = ("+(int)x+", "+(int)y+")\" ;\n");
				outfile.write("}\n");

			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	/**
	  * This function saves the information about a specific dependency
	  * in the table.  It records all of the associated probabilies that
	  * are generated from the different dependencies.  If a particular
	  * node has no dependencies, then it will record the probabilites
	  * for that particular state.
	  */
	private void saveProbabilityDistributionsBIF() {
		Item current;
		for(int loc=0;loc<baynet.size();loc++) {
			current = (Item) baynet.get(loc);

			String name = current.getItem().getBlockName();

			int numparents = current.numParents();
			int rows = ((ChanceBlock)current.getItem()).getRows();
			int columns = ((ChanceBlock)current.getItem()).getColumns();

			try {
				outfile.write("probability (");
				outfile.write("  \""+name+"\"");

				int i;
				for (i=0;i<numparents;i++) {
					String parent = current.getParent(i).getItem().getBlockName();
					outfile.write("  \""+parent+"\"");
				}
				i++;

				outfile.write(" ) { // "+i+" variable(s) and "+rows*columns+" values\n");

				StringBuffer vals = new StringBuffer();
				vals.append("table");

				for(int j=0;j<rows;j++) {
					for(int k=0;k<columns;k++) {
						double value = ((ChanceBlock) current.getItem()).getValue(j, k);
						vals.append(" " + value);
					}
				}

				// Old Bad code.
				/*
				for(int j=0;j<columns;j++) {
					for(int k=0;k<rows;k++) {
						double value = ((ChanceBlock)current.getItem()).getValue(k,j);
						vals.append(" "+value);
					}
				}
				*/
				vals.append(" ;");

				outfile.write("\t"+vals.toString()+"\n");
				outfile.write("}\n");

			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	/**
	  * This function loads a specific file into the editor.  It determines
	  * what file type has been picked and then it will direct it to the
	  * appropriate functions that need to be called.
	  *
	  * @param filename the name of the file that contains the Bayesian
	  *	Network that needs to be loaded
	  * @return baynet the Bayesian Network that was read in from the file
	  */
	public LinkedList load(String filename) {
		StringBuffer sbuf;

		baynet = new LinkedList();
		sbuf = new StringBuffer();

		// reading the entire file into one big string so the relevant
		// information can be parsed out of it
		try {
			BufferedReader infile = new BufferedReader(new FileReader(filename));
			// do some checking to make sure that it's a valid file
			while(infile.ready()) {
				sbuf.append(infile.readLine());
			}

		} catch(FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}

		// check to see what file format we are working with
		// at this point in time there are only two formats
                //
                // Now I'm extending it so that it can open .dsl file too. (Haipeng Guo, Thursday, Oct. 25)
		String tmp = sbuf.toString();

		if(filename.endsWith("xml")) {
                        // XMLBIF file
                        System.out.println("load XMLBIF .xml file ...");
                        System.out.println("load variables ...");
			loadVariablesXMLBIF(tmp, true);
                        System.out.println("load ProbabilityDistributions ...");
			loadProbabilityDistributionsXMLBIF(tmp, true);
			currenttype = "xml";
                } else if (filename.endsWith("xbn")) {
                        // MicroSoft .xbn file
                        System.out.println("load MicroSoft .xbn file ...");
                        System.out.println("load variables ...");
                        loadVariablesXBN(tmp);
                        System.out.println("load ProbabilityDistributions ...");
                        loadProbabilityDistributionsXBN(tmp);
                 } else if(filename.endsWith("dnet")||(filename.endsWith("DNET"))) {
                         // NorSys Netica .dnet file
                         System.out.println("load Netica .dnet file ...");

                        if((tmp.indexOf("bnet ")>0) && (tmp.indexOf(" influence diagram ")<0))  {
                           System.out.println("load variables ...");
                           loadVariablesDNET(tmp);
                           System.out.println("load ProbabilityDistributions ...");
                           loadProbabilityDistributionsDNET(tmp);
                        } else {
                           System.out.println("It doesn't look like a BBN file!");
                           JOptionPane.showMessageDialog(null,"It looks like a decision network instead of a Bayesian network",":( Sorry, I can't do it!",JOptionPane.INFORMATION_MESSAGE);
                         }
                } else if(filename.endsWith("dsl")){
                        // Genie .dsl file
                        System.out.println("load Genie .dsl file ...");
                        System.out.println("load variables ...");

                        loadVariablesDSL(tmp);
                        System.out.println("load ProbabilityDistributions ...");
                        loadProbabilityDistributionsDSL(tmp);
                } else if(filename.endsWith("net")){
                        // Hugin .net file
                        System.out.println("load Hugin .net file ...");

                        if((tmp.indexOf("decision ")<0) ) {
                            System.out.println("load variables ...");
                            loadVariablesNET(tmp);
                            System.out.println("load ProbabilityDistributions ...");
                            loadProbabilityDistributionsNET(tmp);
                        } else  {
                           System.out.println("It doesn't look like a BBN file!");
                           JOptionPane.showMessageDialog(null,"It looks like a decision network instead of a Bayesian network"," Sorry, I can't do it!  :-( ",JOptionPane.INFORMATION_MESSAGE);
                        }
                } else if(filename.endsWith("dsc")) {
                        // MicroSoft .dsc file
                        System.out.println("load MicroSoft .dsc file ...");
                        System.out.println("load variables ...");
                        loadVariablesDSC(tmp);
                        loadProbabilityDistributionsDSC(tmp);
                } else if(filename.endsWith("ent")||(filename.endsWith("ENT"))) {
                        // Ergo .ENT file
                        System.out.println("load Ergo .ENT file ...");
                        System.out.println("load variables ...");
                        loadVariablesENT(tmp);
                        System.out.println("load ProbabilityDistributions ...");
                        loadProbabilityDistributionsENT(tmp);
		} else if(filename.endsWith("bif")) {

			System.out.println("load BIF file ...");
                        System.out.println("load variables ...");
                        loadVariablesBIF(tmp);
                        System.out.println("load ProbabilityDistributions ...");
			loadProbabilityDistributionsBIF(tmp);
			currenttype = "bif";
		}
		return baynet;
	}


        private void loadVariablesDNET(String sbuf) {

                sbuf = this.cleanDNETheader(sbuf);

                // get inheritanceNodeInfo, it will be attached to every nodeInfo so that it
                // serve as the default values and can be overrided by the definitions inside node sections

                String inheritanceNodeInfo = "";
                // there could be more than one inheritance definition
                Vector inheritanceNodes = new Vector();
                int startNodeheritance, endNodeheritance;
                boolean hasinheritanceNodes = false;

                //System.out.println("sbuf = " + sbuf);


                while(sbuf.indexOf("define node ")>=0) {
                      startNodeheritance = sbuf.indexOf("define node ");
                      endNodeheritance = sbuf.indexOf("}", startNodeheritance+12);
                      inheritanceNodeInfo = sbuf.substring(startNodeheritance+12, endNodeheritance).trim();

                       //System.out.println("inheritanceNodeInfo = " + inheritanceNodeInfo);
                      inheritanceNodes.addElement(inheritanceNodeInfo);
                      sbuf = sbuf.substring(endNodeheritance+1).trim();
                      hasinheritanceNodes = true;
                }

                //System.out.println("sbuf = " + sbuf);


                int startNodes = sbuf.indexOf("node ");

                sbuf = sbuf.substring(startNodes +5).trim();

                int startVar = sbuf.indexOf("node ");
                String tmp = "";
                Vector nodesInfo = new Vector();
                String sName, inheritanceName, tmpInheri;

                while(startVar>0) {
			tmp = sbuf.substring(0,startVar).trim();

                        if(hasinheritanceNodes) {
                          sName = sbuf.substring(0, sbuf.indexOf("{")).trim();
                          inheritanceName = sName.substring(sName.indexOf("(")+1, sName.indexOf(")")).trim();
                          //System.out.println("inheritanceName ="+inheritanceName);
                        // attach proper inheritanceNodeInfo to this node
                          for(int i=0;i<inheritanceNodes.size();i++) {
                              tmpInheri = inheritanceNodes.elementAt(i).toString();
                              tmpInheri = tmpInheri.substring(0, tmpInheri.indexOf("{")).trim();
                              //System.out.println("tmpInheri ="+tmpInheri);
                              if(tmpInheri.equals(inheritanceName)) {
                                  tmp = tmp  + "<inheritance>" +inheritanceNodes.elementAt(i).toString();
                                  System.out.println("add inheritance info!");
                              }
                           }
                        }

                        nodesInfo.addElement(tmp.trim());
                        sbuf = sbuf.substring(startVar+5).trim();
                        startVar = sbuf.indexOf("node ");
		}
                sbuf = sbuf.trim();
                tmp = sbuf;

                // handle the last one
                if(hasinheritanceNodes) {
                     sName = sbuf.substring(0, sbuf.indexOf("{")).trim();
                     inheritanceName = sName.substring(sName.indexOf("(")+1, sName.indexOf(")")).trim();
                     //System.out.println("inheritanceName ="+inheritanceName);
                     for(int i=0;i<inheritanceNodes.size();i++) {
                              tmpInheri = inheritanceNodes.elementAt(i).toString();
                              tmpInheri = tmpInheri.substring(0, tmpInheri.indexOf("{")).trim();
                              //System.out.println("tmpInheri ="+tmpInheri);
                              if(tmpInheri.equals(inheritanceName)) {
                                  tmp = tmp + "<inheritance>" +inheritanceNodes.elementAt(i).toString();
                                  //System.out.println("add inheritance info!");
                              }
                      }
                      nodesInfo.addElement(tmp);

                } else nodesInfo.addElement(sbuf);

                for(int i=0;i<nodesInfo.size();i++) {
                  //System.out.println("i = " + i + " " + nodesInfo.elementAt(i).toString());
                  loadNodeDNET(nodesInfo.elementAt(i).toString());
                }



        }

        private void loadProbabilityDistributionsDNET(String sbuf) {

                                sbuf = this.cleanDNETheader(sbuf);

                // get inheritanceNodeInfo, it will be attached to every nodeInfo so that it
                // serve as the default values and can be overrided by the definitions inside node sections

                String inheritanceNodeInfo = "";
                // there could be more than one inheritance definition
                Vector inheritanceNodes = new Vector();
                int startNodeheritance, endNodeheritance;
                boolean hasinheritanceNodes = false;

                //System.out.println("sbuf = " + sbuf);


                while(sbuf.indexOf("define node ")>=0) {
                      startNodeheritance = sbuf.indexOf("define node ");
                      endNodeheritance = sbuf.indexOf("}", startNodeheritance+12);
                      inheritanceNodeInfo = sbuf.substring(startNodeheritance+12, endNodeheritance).trim();

                       //System.out.println("inheritanceNodeInfo = " + inheritanceNodeInfo);
                      inheritanceNodes.addElement(inheritanceNodeInfo);
                      sbuf = sbuf.substring(endNodeheritance+1).trim();
                      hasinheritanceNodes = true;
                }

                //System.out.println("sbuf = " + sbuf);


                int startNodes = sbuf.indexOf("node ");

                sbuf = sbuf.substring(startNodes +5).trim();

                int startVar = sbuf.indexOf("node ");
                String tmp = "";
                Vector nodesInfo = new Vector();
                String sName, inheritanceName, tmpInheri;

                while(startVar>0) {
			tmp = sbuf.substring(0,startVar).trim();

                        if(hasinheritanceNodes) {
                          sName = sbuf.substring(0, sbuf.indexOf("{")).trim();
                          inheritanceName = sName.substring(sName.indexOf("(")+1, sName.indexOf(")")).trim();
                          //System.out.println("inheritanceName ="+inheritanceName);
                        // attach proper inheritanceNodeInfo to this node
                          for(int i=0;i<inheritanceNodes.size();i++) {
                              tmpInheri = inheritanceNodes.elementAt(i).toString();
                              tmpInheri = tmpInheri.substring(0, tmpInheri.indexOf("{")).trim();
                              //System.out.println("tmpInheri ="+tmpInheri);
                              if(tmpInheri.equals(inheritanceName)) {
                                  tmp = tmp  + "<inheritance>" +inheritanceNodes.elementAt(i).toString();
                                  //System.out.println("add inheritance info!");
                              }
                           }
                        }

                        nodesInfo.addElement(tmp.trim());
                        sbuf = sbuf.substring(startVar+5).trim();
                        startVar = sbuf.indexOf("node ");
		}
                sbuf = sbuf.trim();
                tmp = sbuf;

                // handle the last one
                if(hasinheritanceNodes) {
                     sName = sbuf.substring(0, sbuf.indexOf("{")).trim();
                     inheritanceName = sName.substring(sName.indexOf("(")+1, sName.indexOf(")")).trim();
                     //System.out.println("inheritanceName ="+inheritanceName);
                     for(int i=0;i<inheritanceNodes.size();i++) {
                              tmpInheri = inheritanceNodes.elementAt(i).toString();
                              tmpInheri = tmpInheri.substring(0, tmpInheri.indexOf("{")).trim();
                              //System.out.println("tmpInheri ="+tmpInheri);
                              if(tmpInheri.equals(inheritanceName)) {
                                  tmp = tmp + "<inheritance>" +inheritanceNodes.elementAt(i).toString();
                                  //System.out.println("add inheritance info!");
                              }
                      }
                      nodesInfo.addElement(tmp);

                } else nodesInfo.addElement(sbuf);

                for(int i=0;i<nodesInfo.size();i++) {
                  //System.out.println("i = " + i + " " + nodesInfo.elementAt(i).toString());
                  loadDependenciesDNET(nodesInfo.elementAt(i).toString());
                }
        }

        private void loadNodeDNET(String sbuf) {

                String blockName;
                Vector statesList = new Vector();
                int xcoordinate, ycoordinate;

                String sName = sbuf.substring(0, sbuf.indexOf("{")).trim();
                if(sName.indexOf("(")>0) {
                   blockName = sName.substring(0, sName.indexOf("(")).trim();
                   sbuf = sbuf.substring(sbuf.indexOf("{") );
                } else  blockName =  sName;

                //System.out.println("blockName = " + blockName);

                int startCenter, endCenter;
                String sCenter = "";
                startCenter = sbuf.indexOf("center =")+8;
                endCenter = sbuf.indexOf(")", startCenter);
                sCenter = sbuf.substring(startCenter, endCenter).trim();
                sCenter = sCenter.substring(1);

                xcoordinate = Integer.parseInt(sCenter.substring(0,sCenter.indexOf(",")).trim());
                ycoordinate = Integer.parseInt(sCenter.substring(sCenter.indexOf(",")+1, sCenter.length()).trim());

                int startStates = sbuf.indexOf("states =")+8;
                int endStates = sbuf.indexOf(")", startStates);
                String statesInfo = "";
                statesInfo = sbuf.substring(startStates, endStates).trim();
                statesInfo = statesInfo.substring(1);
                //System.out.println("statesInfo = " + statesInfo);

                while(statesInfo.indexOf(",")>0) {
                      statesList.addElement(statesInfo.substring(0, statesInfo.indexOf(",")).trim());
                      statesInfo = statesInfo.substring(statesInfo.indexOf(",")+1).trim();
                 }

                 statesList.addElement(statesInfo);

                 this.addItemToBayNet(blockName,xcoordinate,ycoordinate,statesList);

        }

        private void loadDependenciesDNET(String sbuf) {

                String blockName;
                Vector parentList = new Vector();
                String probabilitiesList = "";

                String sName = sbuf.substring(0, sbuf.indexOf("{")).trim();
                if(sName.indexOf("(")>0)
                   blockName = sName.substring(0, sName.indexOf("(")).trim();
                else  blockName =  sName;

                int startParents = sbuf.indexOf("parents =");
                int endParents;
                if(startParents>0) {
                  endParents = sbuf.indexOf(")", startParents);
                  String parentsInfo = "";
                  parentsInfo = sbuf.substring(startParents+10, endParents).trim();
                  //System.out.println("parentsInfo = " + parentsInfo.toString());
                  parentsInfo = parentsInfo.substring(1).trim();
                  //System.out.println("parentsInfo = " + parentsInfo);
                  if(!parentsInfo.equals("")) {
                     while(parentsInfo.indexOf(",")>0) {
                          parentList.addElement(parentsInfo.substring(0, parentsInfo.indexOf(",")));
                          parentsInfo = parentsInfo.substring(parentsInfo.indexOf(",")+1).trim();
                     }
                     parentList.addElement(parentsInfo);
                  }

                 }

                 //System.out.println("parentList = " + parentList.toString());

                 int startProbs, endProbs;
                 //System.out.println("sbuf = " + sbuf);
                 startProbs = sbuf.indexOf("probs =")+7;
                 endProbs = sbuf.indexOf(";", startProbs);
                 //System.out.println("startProbs = " + startProbs);
                 //System.out.println("endProbs = " + endProbs);
                 String tmpprobabilities = sbuf.substring(startProbs, endProbs).trim();

                 //System.out.println("tmpprobabilities = " + tmpprobabilities);

                 int startP, endP;
                 startP = tmpprobabilities.indexOf("(");
                 endP = tmpprobabilities.indexOf("//", startP);
                 if(endP<0) endP = tmpprobabilities.length();

                 while(startP>0) {

                    probabilitiesList = probabilitiesList + " " + tmpprobabilities.substring(startP, endP).trim();
                    startP = tmpprobabilities.indexOf("(", endP);
                    endP = tmpprobabilities.indexOf("//", startP);
                    if(endP<0) endP = tmpprobabilities.length();
                    //System.out.println("startP = " + startP);
                    // System.out.println("endP = " + endP);
                    // System.out.println("probabilitiesList = " + probabilitiesList);
                }

                //System.out.println("probabilitiesList = " + probabilitiesList);


                char tmpChar;
                String tmp = "";
                for(int i=0;i<probabilitiesList.length();i++) {
                   tmpChar = probabilitiesList.charAt(i);
                   if(Character.isDigit(tmpChar)||(String.valueOf(tmpChar).equals("."))||(String.valueOf(tmpChar).equals("-"))||(String.valueOf(tmpChar).equals("e"))) {
                       tmp = tmp + String.valueOf(tmpChar);
                   } else {
                       tmpChar = ' ';
                       tmp = tmp + String.valueOf(tmpChar);
                   }
                }


                probabilitiesList = this.replaceTwoSpacesToOne(tmp);
                System.out.println("/t = " + probabilitiesList.indexOf("\t"));

                //System.out.println("blockName = " + blockName);
                //System.out.println("parentList = " + parentList.toString());
                //System.out.println("tmp = " + tmp);
                //System.out.println("probabilitiesList = " + probabilitiesList);

               this.addCPTtoBayNetColumnFirst(blockName, parentList, probabilitiesList, true);

        }


        private void loadVariablesENT(String sbuf) {

                int startNodes = sbuf.indexOf("{Node");
                int endNodes = sbuf.indexOf("{Edge");
                sbuf = sbuf.substring(startNodes +5, endNodes).trim();

                int startVar = sbuf.indexOf("{Node");
                String tmp = "";
                Vector nodesInfo = new Vector();

                while(startVar>0) {
			tmp = sbuf.substring(0,startVar).trim();
                        nodesInfo.addElement(tmp.trim());

                        sbuf = sbuf.substring(startVar+5).trim();
                        startVar = sbuf.indexOf("{Node");
		}
                tmp = sbuf.trim();
                nodesInfo.addElement(tmp);

                for(int i=0;i<nodesInfo.size();i++) {
                  //System.out.println("i = " + i + " " + nodesInfo.elementAt(i).toString());
                  loadNodeENT(nodesInfo.elementAt(i).toString());
                }




        }

        private void loadProbabilityDistributionsENT(String sbuf) {

                String sNodes = sbuf.substring(sbuf.indexOf("{Node")+5, sbuf.indexOf("{Edge")).trim();
                String sEdges = sbuf.substring(sbuf.indexOf("{Edge")+5).trim();
                 //System.out.println("sNodes = " + sNodes);
                  //System.out.println("sEdges = " + sEdges);
                // collect all node information Vector nodesInfo
                int startVar = sNodes.indexOf("{Node");
                String tmp = "";
                Vector nodesInfo = new Vector();
                while(startVar>0) {
			tmp = sNodes.substring(0,startVar).trim();
                        nodesInfo.addElement(tmp.trim());
                        sNodes = sNodes.substring(startVar+5).trim();
                        startVar = sNodes.indexOf("{Node");
		}
                tmp = sNodes.trim();
                nodesInfo.addElement(tmp);

                //System.out.println("nodesInfo size " + nodesInfo.size());
                //collect all edges information into Vector edgesInfo
                int startEdge = sEdges.indexOf("{Edge");
                Vector edgesInfo = new Vector();
                while(startEdge>0) {
			tmp = sEdges.substring(0,startEdge).trim();
                        edgesInfo.addElement(tmp.trim());
                        sEdges = sEdges.substring(startEdge+5).trim();
                        ////System.out.println("sEdges = " + sEdges);
                        startEdge = sEdges.indexOf("{Edge");
		}
                tmp = sEdges.trim();
                edgesInfo.addElement(tmp);
                //System.out.println("edgesInfo size " + edgesInfo.size());

                //attach parents infomation to nodesInfo
                 String nodename = "";
                 String childname = "";
                 int startnodeName, endnodeName, startchildName, endchildName;
                 String tmp1, tmp2;
                for(int i=0;i<nodesInfo.size();i++) {

                    tmp1 = nodesInfo.elementAt(i).toString();
                    startnodeName = tmp1.indexOf("Name") +4;
                    endnodeName = tmp1.indexOf("}", startnodeName);
                    nodename = tmp1.substring(startnodeName, endnodeName).trim();

                    for(int j=0;j<edgesInfo.size();j++) {

                       tmp2 = edgesInfo.elementAt(j).toString();
                       startchildName = tmp2.indexOf("Child") +5;
                       endchildName = tmp2.indexOf("}", startchildName);
                       childname = tmp2.substring(startchildName, endchildName).trim();

                       if(nodename.equals(childname)) {
                          tmp1 = tmp1 + " <Dependencies> " + tmp2;
                          nodesInfo.setElementAt(tmp1, i);
                       }
                    }
                }

                // call loadDependenciesENT() for each node
                for(int i=0;i<nodesInfo.size();i++) {
                  //System.out.println("i = " + i + " " + nodesInfo.elementAt(i).toString());
                  loadDependenciesENT(nodesInfo.elementAt(i).toString());
                }




        }

        private void loadNodeENT(String sbuf) {

                 String blockName;
                 Vector statesList = new Vector();
                 int xcoordinate;
                 int ycoordinate;

                 int startNStates = sbuf.indexOf("{NStates ")+9;
                 int endNStates = sbuf.indexOf("}", startNStates);
                 String numOfStates = sbuf.substring(startNStates, endNStates).trim();

                 int startName, endName;
                 startName = sbuf.indexOf("Name") +4;
                 endName = sbuf.indexOf("}", startName);
                 blockName = sbuf.substring(startName, endName).trim();

                 int startCenter, endCenter;
                 String sCenters = "";
                 startCenter = sbuf.indexOf("Center")+6;
                 endCenter = sbuf.indexOf("}", startCenter);
                 sCenters = sbuf.substring(startCenter, endCenter).trim();

                 xcoordinate = Integer.parseInt(sCenters.substring(0,sCenters.indexOf(" ")).trim());
                 ycoordinate = Integer.parseInt(sCenters.substring(sCenters.indexOf(" ")+1, sCenters.length()).trim());

                 int startStates, endStates;
                 String statesInfo = "";
                 startStates = sbuf.indexOf("{Labels ")+8;
                 endStates = sbuf.indexOf("}", startStates);
                 statesInfo = sbuf.substring(startStates, endStates).trim();
                 statesInfo = statesInfo.substring(statesInfo.indexOf(numOfStates)+numOfStates.length()).trim();
                 //System.out.println("statesInfo = " + statesInfo);

                 while(statesInfo.indexOf("\t")>0) {
                      statesList.addElement(statesInfo.substring(0, statesInfo.indexOf("\t")));
                      statesInfo = statesInfo.substring(statesInfo.indexOf("\t")).trim();
                 }

                 statesList.addElement(statesInfo);

                 this.addItemToBayNet(blockName,xcoordinate,ycoordinate,statesList);
        }

         private void loadDependenciesENT(String sbuf) {

                 String blockName;
                 Vector parentList = new Vector();
                 String probabilitiesList ="";

                 //get blockName
                 int startName, endName;
                 startName = sbuf.indexOf("Name") +4;
                 endName = sbuf.indexOf("}", startName);
                 blockName = sbuf.substring(startName, endName).trim();

                 //get parentList
                 int startParent, endParent;
                 String tmp="";
                 String parentname = "";
                 if(sbuf.indexOf("<Dependencies>")>0) {
                    tmp = sbuf.substring(sbuf.indexOf("<Dependencies>")+14).trim();
                    startParent = tmp.indexOf("{Parent");
                    endParent = tmp.indexOf("}", startParent);
                    while(startParent>=0) {
                         parentname = tmp.substring(startParent+7, endParent).trim();
                         parentList.addElement(parentname);
                         startParent = tmp.indexOf("{Parent", endParent);
                         endParent = tmp.indexOf("}", startParent);
                    }
                 }

                 // get probabilitiesList
                 int startProbs, endProbs;
                 startProbs = sbuf.indexOf("{Probabilities");
                 endProbs = sbuf.indexOf("}", startProbs);
                 tmp = sbuf.substring(startProbs+14, endProbs).trim();

                 probabilitiesList = tmp.substring(tmp.indexOf("\t")+1).trim();
                 tmp = "";
                 char tmpChar;

                 for(int i=0;i<probabilitiesList.length();i++) {
                   tmpChar = probabilitiesList.charAt(i);
                   if((!Character.isDigit(tmpChar))&&(!String.valueOf(tmpChar).equals("."))) {
                       tmpChar = ' ';
                       tmp = tmp + String.valueOf(tmpChar);
                   } else {
                       tmp = tmp + String.valueOf(tmpChar);
                   }
                 }

                 probabilitiesList = this.replaceTwoSpacesToOne(tmp);


                // //System.out.println("parentname = " + parentList.toString());
                 ////System.out.println("probabilitiesList = " + probabilitiesList);
                 this.addCPTtoBayNetColumnFirst(blockName, parentList, probabilitiesList, true);

        }

        private void loadVariablesXBN(String sbuf) {

                sbuf = sbuf.substring(sbuf.indexOf("<BNMODEL"), sbuf.indexOf("</BNMODEL>")+10);

                int startVariables;
                int endVariables;
                int startStructure;
                int endStructure;
                int startVar;
		int endVar;

		startVariables = sbuf.indexOf("<VARIABLES");
		endVariables = sbuf.indexOf("</VARIABLES>");

                sbuf = sbuf.substring(startVariables, endVariables);

                startVar = sbuf.indexOf("<VAR ");
                endVar = sbuf.indexOf("</VAR>");

		while(startVar>0) {
			loadNodeXBN(sbuf.substring(startVar, endVar));
			startVar = sbuf.indexOf("<VAR ",endVar);
			endVar = sbuf.indexOf("</VAR>",startVar);
		}

        }

        private void loadNodeXBN(String sbuf) {

                // need the following four variables' information to create & add current node
		String blockName;
                Vector stateList = new Vector();
                int xcoordinate;
		int ycoordinate;

                String tmp = sbuf.substring(sbuf.indexOf("<VAR "), sbuf.indexOf(">", 1));
                ////System.out.println("tmp = " + tmp);
                int startName =  tmp.indexOf("NAME=")+5;
                int endName = tmp.indexOf("TYPE");
                ////System.out.println("startName = " + startName);
                ////System.out.println("endName = " + endName);
                blockName = tmp.substring(startName, endName).trim();
                blockName = blockName.substring(1,blockName.length()-1).trim();

                //System.out.println("blockName = " + blockName);
		int startXCoordinates = tmp.indexOf("XPOS=\"" )+6;
                int startYCoordinates = tmp.indexOf("YPOS=\"")+6;
		int endXCoordinates = tmp.indexOf("\"",startXCoordinates);
                int endYCoordinates = tmp.indexOf("\"",startYCoordinates);

		xcoordinate = new Integer(sbuf.substring(startXCoordinates,endXCoordinates).trim()).intValue();
		ycoordinate = new Integer(sbuf.substring(startYCoordinates,endYCoordinates).trim()).intValue();

                xcoordinate = xcoordinate/20 - 100;
                ycoordinate = ycoordinate/20 - 100;

                int startStatename;
                int endStatename;

                startStatename = sbuf.indexOf("<STATENAME>");
                endStatename = sbuf.indexOf("</STATENAME>");
               // sbuf = sbuf.substring(startStatename+11);

                while(endStatename > 0) {
                   stateList.addElement(sbuf.substring(startStatename+11,endStatename).trim());
                   sbuf = sbuf.substring(endStatename+12).trim();
                   startStatename = sbuf.indexOf("<STATENAME>");
                   endStatename = sbuf.indexOf("</STATENAME>");
		}

                //System.out.println("stateList = " + stateList.toString());
                this.addItemToBayNet(blockName,xcoordinate,ycoordinate,stateList);

        }

        private void loadProbabilityDistributionsXBN(String sbuf) {
                //System.out.println("entering loadProbabilityDistributionsXBN");

                String structuresInfo ;
                String distributionsInfo;
                int startStructures;
                int endStructures;
                int startDistributions, endDistributions;

                startStructures = sbuf.indexOf("<STRUCTURE>");
                endStructures = sbuf.indexOf("</STRUCTURE>");
                structuresInfo = sbuf.substring(startStructures, endStructures);

                startDistributions = sbuf.indexOf("<DISTRIBUTIONS>");
                endDistributions = sbuf.indexOf("</DISTRIBUTIONS>");
                distributionsInfo = sbuf.substring(startDistributions+15, endDistributions).trim();
                //distributionsInfo = distributionsInfo.substring(5);

                //System.out.println("distributionsInfo = " + distributionsInfo);

                int startDIST = distributionsInfo.indexOf("<DIST ");
                int endDIST= distributionsInfo.indexOf("</DIST>");
                //System.out.println("startDIST = " + startDIST);
                //System.out.println("endDIST = " + endDIST);


                while(startDIST >= 0) {
			loadDependenciesXBN(distributionsInfo.substring(startDIST,endDIST));
                        startDIST = distributionsInfo.indexOf("<DIST ",endDIST);
			endDIST = distributionsInfo.indexOf("</DIST>",startDIST);
		}
        }

        private void loadDependenciesXBN(String sbuf) {
                //System.out.println("entering loadDependenciesXBN");
                //System.out.println("distributionsInfo = " + sbuf);

                String blockName = "";
                Vector parentList = new Vector();
                String probabilitiesList ="";

                int startprivateName, endprivateName;
                int startCondset, endCondset;
                int startDpis, endDpis;
                int startCondelem;

                startprivateName = sbuf.indexOf("PRIVATE NAME");
                endprivateName = sbuf.indexOf("/>",startprivateName);
                blockName = sbuf.substring(startprivateName+14, endprivateName-1).trim();
                //System.out.println("blockName = "  + blockName);

                startCondset = sbuf.indexOf("CONDSET>");
                endCondset = sbuf.indexOf("</CONDSET>");

                if(!(startCondset<0)) {
                  String sCondset = sbuf.substring(startCondset+8, endCondset);
                  startCondelem = sCondset.indexOf("<CONDELEM ");
                  while(startCondelem>=0) {
                     parentList.addElement(sCondset.substring(startCondelem+16, sCondset.indexOf("\"/>",startCondelem)).trim());
                     startCondelem = sCondset.indexOf("<CONDELEM ",startCondelem+16);
                  }
                }
                //System.out.println("parentList = "  + parentList.toString());

                startDpis = sbuf.indexOf("<DPIS>")+6;
                endDpis = sbuf.indexOf("</DPIS>");
                String sDpis = sbuf.substring(startDpis, endDpis);
                //System.out.println("sDpis = "  + sDpis);

                int startDPI, endDPI;
                int startProbability, endProbability;
                startDPI = sDpis.indexOf("<DPI");
                endDPI = sDpis.indexOf("</DPI>");
                startProbability = sDpis.indexOf(">", startDPI)+1;
                endProbability = endDPI;
                while(startDPI>=0) {
                     probabilitiesList = probabilitiesList + " " + sDpis.substring(startProbability, endProbability).trim();
                     startDPI = sDpis.indexOf("<DPI", endDPI);
                     endDPI = sDpis.indexOf("</DPI>", startDPI);
                     startProbability = sDpis.indexOf(">", startDPI)+1;
                     endProbability = endDPI;
                }
                probabilitiesList = probabilitiesList.trim();
                //System.out.println("probabilitiesList = "  + probabilitiesList);

                this.addCPTtoBayNetColumnFirst(blockName, parentList, probabilitiesList, true);
        }

        private void loadVariablesDSC(String sbuf) {

                sbuf = sbuf.substring(sbuf.indexOf("node ")+5, sbuf.indexOf("probability")).trim();
                int startVar = 0;
		int endVar;
                String tmp = "";
                Vector nodesInfo = new Vector();

		endVar = sbuf.indexOf("node ", startVar);

		while(endVar>0) {
			tmp = sbuf.substring(0,endVar).trim();
                        sbuf = sbuf.substring(endVar+4,sbuf.length());
                        endVar = sbuf.indexOf("node ", startVar);
                        //tmp = tmp.substring(tmp.indexOf("{")+1,tmp.length()-1);
                        nodesInfo.addElement(tmp.trim());
		}
                tmp = sbuf.trim();
                nodesInfo.addElement(tmp);
                //nodesInfo.addElement(tmp.substring(tmp.indexOf("{")+1,tmp.length()-1).trim());


                for(int i=0;i<nodesInfo.size();i++) {
                  //System.out.println("i = " + i + " " + nodesInfo.elementAt(i).toString());
                  loadNodeDSC(nodesInfo.elementAt(i).toString());
                }

        }

        private void loadNodeDSC(String sbuf) {
                String blockName;
                Vector stateList = new Vector();
                int xcoordinate = 0;
                int ycoordinate = 0;
                ////System.out.println("sbuf = " + sbuf);

                blockName = sbuf.substring(0,sbuf.indexOf("{")).trim();
                //System.out.println("blockName = " + blockName);

                int startPosition, endPosition;
                startPosition = sbuf.indexOf("position")+8;
                endPosition = sbuf.indexOf(")",startPosition);
                String tmp = sbuf.substring(startPosition, endPosition).trim();
                if(tmp.indexOf("=")==0) {
                  tmp = tmp.replace('=',' ');
                  tmp = tmp.trim();
                }
                if(tmp.indexOf(":")==0) {
                  tmp = tmp.replace(':',' ');
                  tmp = tmp.trim();
                }
                tmp = tmp.substring(1);
                //System.out.println("tmp = " + tmp);
                xcoordinate = Integer.parseInt(tmp.substring(0,tmp.indexOf(",")).trim());
                ycoordinate = Integer.parseInt(tmp.substring(tmp.indexOf(",")+1, tmp.length()).trim());
                xcoordinate = xcoordinate/20 - 500;
                ycoordinate = ycoordinate/20 - 500;
                String stateInfo = sbuf.substring(sbuf.indexOf("discrete["), sbuf.indexOf("position")).trim();
                stateInfo = stateInfo.substring(stateInfo.indexOf("{")+1, stateInfo.indexOf("}")).trim();
                while(stateInfo.indexOf("\"")==0) {
                    ////System.out.println("stateInfo = " + stateInfo);
                    stateInfo = stateInfo.substring(1);
                    stateList.addElement(stateInfo.substring(0,stateInfo.indexOf("\"")));
                    if(!(stateInfo.indexOf("\"") == stateInfo.length()-1))
                     stateInfo = stateInfo.substring(stateInfo.indexOf("\"")+2).trim();
                    ////System.out.println("stateInfo = " + stateInfo);
                }
                ////System.out.println("stateInfo = " + stateInfo);
                //stateList = stateList.substring(1,stateList.length()-1).trim();
                // stateList may contain two space char --- "  ", need to repace it with " "
                //tmp = stateList.substring(0,stateList.indexOf(" "));
                //stateList = stateList.substring(stateList.indexOf(" "), stateList.length()).trim();
                //while(stateList.indexOf(" ")>0) {
                  //tmp = tmp + " " + stateList.substring(0,stateList.indexOf(" "));
                  //stateList = stateList.substring(stateList.indexOf(" "), stateList.length()).trim();
                //}
                //tmp = tmp + " " + stateList.trim();
                //stateList = tmp;
                //System.out.println("stateList = " + stateList);
                //System.out.println("x = " + xcoordinate + " y = " + ycoordinate);
                //xcoordinate =  this.getNETinfo(sbuf, "position: ", ';');

                this.addItemToBayNet(blockName, xcoordinate, ycoordinate, stateList);

        }

        private void loadProbabilityDistributionsDSC(String sbuf) {

                sbuf = sbuf.substring(sbuf.indexOf("probability")+11, sbuf.length());
                //System.out.println("sbuf = " + sbuf);

                Vector probabilityInfo = new Vector();
                int startVar = 0;
		int endVar;
                String tmp = "";

                endVar = sbuf.indexOf("probability", startVar);
                while(endVar>0) {

                        tmp = sbuf.substring(0,endVar);
                        sbuf = sbuf.substring(endVar+11,sbuf.length());
                        endVar = sbuf.indexOf("probability", startVar);
                        probabilityInfo.addElement(tmp.trim());

		}
                tmp = sbuf;
                probabilityInfo.addElement(tmp.trim());

                for(int i=0;i<probabilityInfo.size();i++) {
                  //System.out.println("i = " + i + " " + probabilityInfo.elementAt(i).toString());
                  loadDependenciesDSC(probabilityInfo.elementAt(i).toString());
                }
        }

        private void loadDependenciesDSC(String sbuf) {

                String blockName;
                Vector parentList = new Vector();
                String probabilitiesList = "";
                String parentInfo = "";

                //System.out.println("loadDependenciesDSC ... ");

                if(sbuf.indexOf("|")<0) {  // root node
                  blockName = sbuf.substring(sbuf.indexOf("(")+1, sbuf.indexOf(")")).trim();
                  ////System.out.println("blockName = " + blockName);
                  probabilitiesList = sbuf.substring(sbuf.indexOf("{")+1,sbuf.indexOf(";")).trim();
                  ////System.out.println("probabilitiesList =!!! " + probabilitiesList);
                  probabilitiesList = this.replaceTwoSpacesToOne(probabilitiesList.replace(',',' ')) ;
                  ////System.out.println("probabilitiesList = " + probabilitiesList);
                }
                else {
                  blockName = sbuf.substring(sbuf.indexOf("(")+1, sbuf.indexOf("|")).trim();
                  parentInfo = sbuf.substring(sbuf.indexOf("|")+1, sbuf.indexOf(")")).trim();
                  String tmp = "";
                  while(parentInfo.indexOf(',')>0) {
                      parentList.addElement(parentInfo.substring(0,parentInfo.indexOf(',')).trim());
                      parentInfo = parentInfo.substring(parentInfo.indexOf(',')+1).trim();
                  }
                  parentList.addElement(parentInfo);
                  //System.out.println("parentList = " + parentList.toString());


                  int startP, endP;
                  startP = sbuf.indexOf("):") ;
                  endP = sbuf.indexOf(";", startP);

                  //tmp = sbuf.substring(startP, endP).trim();
                  ////System.out.println("tmp = " + tmp);
                  while(startP>0) {
                    tmp = sbuf.substring(startP+2, endP).trim();
                    probabilitiesList = probabilitiesList +", "+ tmp;
                    ////System.out.println("probabilitiesList = " + probabilitiesList);

                    startP = sbuf.indexOf("):", endP);
                    endP = sbuf.indexOf(";", startP);
                    //tmp = tmp.substring(tmp.indexOf(";")+1).trim();
                    ////System.out.println("tmp = " + tmp);
                    ////System.out.println("probabilitiesList = " + probabilitiesList);
                  }

                  probabilitiesList = probabilitiesList.substring(1).trim();
                  //System.out.println("probabilitiesList = " + probabilitiesList);
                  probabilitiesList = probabilitiesList.replace(',',' ');
                  probabilitiesList = this.replaceTwoSpacesToOne(probabilitiesList);
                  //System.out.println("probabilitiesList = " + probabilitiesList);

/*                  blockName = sbuf.substring(sbuf.indexOf("(")+1, sbuf.indexOf("|")).trim();
                  parentInfo = sbuf.substring(sbuf.indexOf("|")+1, sbuf.indexOf(")")).trim();
                  //parentList = this.trimexcess(parentList);

                  probabilitiesList = this.getNETinfo(sbuf, "{", '}');
                // need to clean probabilitiesList
                  String tmp = "";
                  while(probabilitiesList.indexOf(":")>0) {
                    tmp = tmp + " " + probabilitiesList.substring(probabilitiesList.indexOf(":")+1,probabilitiesList.indexOf(";"));
                    probabilitiesList = probabilitiesList.substring(probabilitiesList.indexOf(";")+1,probabilitiesList.length()).trim();
                  }
                  probabilitiesList = tmp.trim();
*/
                }

                //System.out.println("blockName = " + blockName);
                //System.out.println("parentList = " + parentList.toString());
                //System.out.println("probabilitiesList = " + probabilitiesList);

               this.addCPTtoBayNetColumnFirst(blockName, parentList, probabilitiesList, true);

        }

        private void loadVariablesNET(String sbuf) {
                sbuf = sbuf.substring(sbuf.indexOf("node ")+5, sbuf.indexOf("potential ")).trim();
                int startVar = 0;
		int endVar;
                String tmp = "";
                Vector nodesInfo = new Vector();

		endVar = sbuf.indexOf("node ", startVar);

		while(endVar>0) {
			tmp = sbuf.substring(0,endVar).trim();
                        sbuf = sbuf.substring(endVar+4,sbuf.length());
                        endVar = sbuf.indexOf("node ", startVar);
                        //tmp = tmp.substring(tmp.indexOf("{")+1,tmp.length()-1);
                        nodesInfo.addElement(tmp.trim());
		}
                tmp = sbuf.trim();
                nodesInfo.addElement(tmp);
                //nodesInfo.addElement(tmp.substring(tmp.indexOf("{")+1,tmp.length()-1).trim());


                for(int i=0;i<nodesInfo.size();i++) {
                  //System.out.println("i = " + i + " " + nodesInfo.elementAt(i).toString());
                  loadNodeNET(nodesInfo.elementAt(i).toString());
                }

        }

        private void loadNodeNET(String sbuf) {
                //System.out.println("loadNodeNET...");

                String nodename = sbuf.substring(0,sbuf.indexOf("{"));
                int startStates = sbuf.indexOf("states =");
                int endstates = sbuf.indexOf(";",startStates);
                Vector statesList = new Vector();

                String tmpstates = sbuf.substring(startStates+8,endstates).trim();// ("yes" "no")
                tmpstates = tmpstates.substring(1,tmpstates.length()-1).trim(); // "yes" "no"
               // //System.out.println("tmpstates = " + tmpstates);
                tmpstates = tmpstates.substring(1).trim();  // yes" "no"
               // //System.out.println("tmpstates = " + tmpstates);

                while(tmpstates.indexOf("\" ")>0) {
                      statesList.addElement(tmpstates.substring(0,tmpstates.indexOf("\" ")).trim());
                       tmpstates = tmpstates.substring(tmpstates.indexOf(" \"")+2).trim();
                      //System.out.println("tmpstates = " + tmpstates);
                }
                statesList.addElement(tmpstates.substring(0,tmpstates.indexOf("\"")).trim());

                String position = this.getNETinfo(sbuf, "position =", ';');
                if(position.equals(""))
                  position = "0 0";

                //System.out.println("nodename = "+ nodename);
                //System.out.println("statesList = "+ statesList.toString());
                //System.out.println("position = "+ position);

                int xcoordinate = Integer.parseInt(position.substring(0,position.indexOf(" ")).trim());
                int ycoordinate = Integer.parseInt(position.substring(position.indexOf(" ")+1, position.length()).trim());
                ycoordinate = ycoordinate - 2*(ycoordinate-150) + 150;

                String blockName = nodename;

                //System.out.println("xcoordinate = "+ xcoordinate);
                //System.out.println("ycoordinate = "+ ycoordinate);
                this.addItemToBayNet(blockName, xcoordinate, ycoordinate, statesList);

        }

        private void loadProbabilityDistributionsNET(String sbuf) {
                sbuf = sbuf.substring(sbuf.indexOf("potential ")+10, sbuf.length());
                //System.out.println("sbuf = " + sbuf);

                Vector potentislInfo = new Vector();
                int startVar = 0;
		int endVar;
                String tmp = "";

                endVar = sbuf.indexOf("potential ", startVar);
                while(endVar>0) {

                        tmp = sbuf.substring(0,endVar);
                        sbuf = sbuf.substring(endVar+10,sbuf.length());
                        endVar = sbuf.indexOf("potential ", startVar);
                        potentislInfo.addElement(tmp.trim());

		}
                tmp = sbuf;
                potentislInfo.addElement(tmp.trim());

                for(int i=0;i<potentislInfo.size();i++) {
                  //System.out.println("i = " + i + " " + potentislInfo.elementAt(i).toString());
                  loadDependenciesNET(potentislInfo.elementAt(i).toString());
                }


        }

        private void loadDependenciesNET(String sbuf) {
                //System.out.println("entering loadDependenciesNET ... ");

                String blockName;
                Vector parentList = new Vector();
                String tmpparentList="";
                String probabilitiesList="";

                if(sbuf.indexOf("|")<0) {
                  blockName = sbuf.substring(1, sbuf.indexOf(")")).trim();
                }
                else {
                  blockName = sbuf.substring(1, sbuf.indexOf("|")).trim();
                  tmpparentList = sbuf.substring(sbuf.indexOf("|")+1, sbuf.indexOf(")")).trim();
                  }

                StringTokenizer t1 = new StringTokenizer(tmpparentList," ");
                int count = t1.countTokens();
                for(int i=0;i<count;i++)
                 parentList.addElement(t1.nextToken().trim());

                int startData = sbuf.indexOf("data =")+6;
                int endData = sbuf.indexOf("}",startData);
                String tmpprobabilities = sbuf.substring(startData, endData).trim();
                //System.out.println("tmpprobabilities = " + tmpprobabilities);
                int startComments = tmpprobabilities.indexOf("%");

                if(startComments>0) {
                    probabilitiesList = tmpprobabilities.substring(0, startComments).trim();

                    tmpprobabilities = tmpprobabilities.substring(tmpprobabilities.indexOf("%")).trim();
                    //System.out.println("tmpprobabilities = " + tmpprobabilities);

                    int startProbs = tmpprobabilities.indexOf("(");
                    int endProbs = tmpprobabilities.indexOf("%", startProbs);
                    while(startProbs>=0) {
                         probabilitiesList = probabilitiesList + tmpprobabilities.substring(startProbs, endProbs);
                         startProbs = tmpprobabilities.indexOf("(", endProbs);
                         endProbs = tmpprobabilities.indexOf("%", startProbs);
                    }

                }
                else  probabilitiesList = probabilitiesList + tmpprobabilities;
                char tmpChar;
                String tmp = "";
                for(int i=0;i<probabilitiesList.length();i++) {
                   tmpChar = probabilitiesList.charAt(i);
                   if((!Character.isDigit(tmpChar))&&(!String.valueOf(tmpChar).equals("."))) {
                       tmpChar = ' ';
                       tmp = tmp + String.valueOf(tmpChar);
                   } else {
                       tmp = tmp + String.valueOf(tmpChar);
                   }
                }


                probabilitiesList = tmp;
                probabilitiesList = this.replaceTwoSpacesToOne(probabilitiesList);
                //System.out.println("blockName = " + blockName);
                //System.out.println("parentList = " + parentList);
                //System.out.println("probabilitiesList = " + probabilitiesList);

                this.addCPTtoBayNetColumnFirst(blockName, parentList, probabilitiesList, true);
        }

        private void loadVariablesDSL(String sbuf) {
                //System.out.println("loadVariablesDSL...");
                sbuf = sbuf.substring(sbuf.indexOf("node"),sbuf.length()).trim();
                sbuf = sbuf.substring(4,sbuf.lastIndexOf("}")).trim();
                //System.out.println("sbuf = " + sbuf);
                sbuf = this.clearDSLcomments(sbuf, "COMMENT =");
                //System.out.println("after clean comments, sbuf = " + sbuf);


                int startVar = 0;
		int endVar;
                String tmp = "";
                Vector nodesInfo = new Vector();

		endVar = sbuf.indexOf("node ", startVar);

		while(endVar>0) {
			//loadNodeDSL(sbuf.substring(startVar, endVar));
                        tmp = sbuf.substring(0,endVar);
                        ////System.out.println("tmp = " + tmp);
                        sbuf = sbuf.substring(endVar+4,sbuf.length());
                        endVar = sbuf.indexOf("node ", startVar);
                        nodesInfo.addElement(tmp.trim());

		}
                tmp = sbuf;
                nodesInfo.addElement(tmp.trim());


                for(int i=0;i<nodesInfo.size();i++) {
                  //System.out.println("i = " + i + " " + nodesInfo.elementAt(i).toString());
                  loadNodeDSL(nodesInfo.elementAt(i).toString());
                }

        }

	/**
	  * Loads all of the nodes that are located into the file.  This
	  * function seperates all of the nodes and then calls another
	  * function that well parse each individual data stream and pull
	  * out the relevant information.
	  *
	  * @param sbuf a string including all information of th node being parsered
	  */
        private void loadNodeDSL(String sbuf) {

                String blockName;
                int xcoordinate;
                int ycoordinate;
                Vector stateList = new Vector();
                String probabilitiesList;
              ////System.out.println("entering loadNodeDSL...");

                blockName = this.getDSLinfo(sbuf, "ID =");
                xcoordinate = Integer.parseInt(this.getDSLinfo(sbuf, "CENTER_X ="));
                ycoordinate = Integer.parseInt(this.getDSLinfo(sbuf, "CENTER_Y ="));
                String tmpstateList = sbuf.substring(sbuf.indexOf("NAMESTATES =")+12, sbuf.indexOf(";",sbuf.indexOf("NAMESTATES ="))).trim();
                tmpstateList = tmpstateList.substring(1,tmpstateList.length()-1).trim();
                while(tmpstateList.indexOf(",")>0) {
                     stateList.addElement(tmpstateList.substring(0,tmpstateList.indexOf(",")).trim());
                     tmpstateList = tmpstateList.substring(tmpstateList.indexOf(",")+1).trim();
                }
                stateList.addElement(tmpstateList.trim());

                probabilitiesList = this.getDSLinfo(sbuf, "PROBABILITIES =");

                ////System.out.println("blockname = " + blockName);
                ////System.out.println("X = " + xcoordinate +", Y=" + ycoordinate);
                ////System.out.println("stateList = " + stateList);
               // //System.out.println("probabilitiesList = " + probabilitiesList);

                this.addItemToBayNet(blockName, xcoordinate, ycoordinate, stateList);
        }

        private void loadDependenciesDSL(String sbuf) {


                ////System.out.println("entering loadDependenciesDSL ... ");
                String blockName = this.getDSLinfo(sbuf, "ID =");

                Vector parentList = new Vector();

                String tempparentList = sbuf.substring(sbuf.indexOf("PARENTS =")+9, sbuf.indexOf(";",sbuf.indexOf("PARENTS ="))).trim();
                ////System.out.println("tempparentList = " + tempparentList);
                tempparentList = tempparentList.substring(1,tempparentList.length()-1).trim();
                ////System.out.println("tempparentList = " + tempparentList);
                if(!tempparentList.trim().equals("")) {
                while(tempparentList.indexOf(",")>0) {
                     parentList.addElement(tempparentList.substring(0,tempparentList.indexOf(",")).trim());
                     tempparentList = tempparentList.substring(tempparentList.indexOf(",")+1).trim();
                }
                parentList.addElement(tempparentList.trim());
                }



                String probabilitiesList = this.getDSLinfo(sbuf, "PROBABILITIES =");
                String resultingStates = this.getDSLinfo(sbuf,"RESULTINGSTATES =");
                // have to use resultingStates to fill probabilitiesList
                // put resultingStates into  a Vector
                Vector resultingList = new Vector();
                StringTokenizer t1 = new StringTokenizer(resultingStates," ");
                int count = t1.countTokens();
                int resultingListSize = count;
                for(int i=0;i<count;i++)
                 resultingList.addElement(t1.nextToken().trim());

                 Vector stateList = new Vector();
                 String tmpstateList = sbuf.substring(sbuf.indexOf("NAMESTATES =")+12, sbuf.indexOf(";",sbuf.indexOf("NAMESTATES ="))).trim();
                tmpstateList = tmpstateList.substring(1,tmpstateList.length()-1).trim();
                while(tmpstateList.indexOf(",")>0) {
                     stateList.addElement(tmpstateList.substring(0,tmpstateList.indexOf(",")).trim());
                     tmpstateList = tmpstateList.substring(tmpstateList.indexOf(",")+1).trim();
                }
                stateList.addElement(tmpstateList.trim());
/*
                 // count the number of states, put them into another vector
                Vector states = new Vector();
                t1 = new StringTokenizer(stateList," ");
                count = t1.countTokens();
                for(int i=0;i<count;i++) {
                   states.addElement(t1.nextToken().trim());
                }
                //System.out.println("count = " + count);
*/
                // now generating probabilitiesList from resultingList
                // note: first by column, then by row !!!
                //System.out.println("resultingListSize = " + resultingListSize);
                if(probabilitiesList.length()==0) {
                  //System.out.println("null probabilities !");
                  for(int j=0;j<resultingListSize;j++) {
                        for(int i=0;i<stateList.size();i++) {
                        double d;
                        if(resultingList.elementAt(j).toString().equals(stateList.elementAt(i))) {
                          d = 1;
                        }
                        else d =0;
                        probabilitiesList = probabilitiesList + Double.toString(d) + " ";
                     }
                  }
                 }

                 //System.out.println("blockname = " + blockName);
                ////System.out.println("X = " + xcoordinate +", Y=" + ycoordinate);
                //System.out.println("parentList = " + parentList);
                //System.out.println("stateList = " + stateList);
                //System.out.println("probabilitiesList = " + probabilitiesList);

                this.addCPTtoBayNetColumnFirst(blockName, parentList, probabilitiesList, true);

        }


        private void loadProbabilityDistributionsDSL(String sbuf) {
                ////System.out.println("loadProbabilityDistributionDSL...");

                sbuf = sbuf.substring(sbuf.indexOf("node"),sbuf.length()).trim();
                sbuf = sbuf.substring(4,sbuf.lastIndexOf("}")).trim();
                ////System.out.println("sbuf = " + sbuf);
                sbuf = this.clearDSLcomments(sbuf, "COMMENT =");
                ////System.out.println("after clean comments, sbuf = " + sbuf);


                int startVar = 0;
		int endVar;
                String tmp = "";
                Vector nodesInfo = new Vector();

		endVar = sbuf.indexOf("node ", startVar);

		while(endVar>0) {
			//loadNodeDSL(sbuf.substring(startVar, endVar));
                        tmp = sbuf.substring(0,endVar);
                        ////System.out.println("tmp = " + tmp);
                        sbuf = sbuf.substring(endVar+4,sbuf.length());
                        endVar = sbuf.indexOf("node ", startVar);
                        nodesInfo.addElement(tmp.trim());

		}
                tmp = sbuf;
                nodesInfo.addElement(tmp.trim());

                for(int i=0;i<nodesInfo.size();i++) {
                  //System.out.println("i = " + i + " " + nodesInfo.elementAt(i).toString());
                  loadDependenciesDSL(nodesInfo.elementAt(i).toString());
                }


        }

	/**
	  * Loads all of the nodes that are located into the file.  This
	  * function seperates all of the nodes and then calls another
	  * function that well parse each individual data stream and pull
	  * out the relevant information.
	  *
	  * @param sbuf a string of the entire file
	  */
	private void loadVariablesXMLBIF(String sbuf, boolean currentVer) {
		int startVar;
		int endVar;

		startVar = sbuf.indexOf("<VARIABLE");
		endVar = sbuf.indexOf("</VARIABLE>");

		while(startVar>0) {
			loadNodeXMLBIF(sbuf.substring(startVar, endVar), currentVer);
			startVar = sbuf.indexOf("<VARIABLE",endVar);
			endVar = sbuf.indexOf("</VARIABLE>",startVar);
		}
	}

	/**
	  * This function gets all of the information for one node
	  * and creates a new Block that is a representation of this node
	  * and adds it to the Bayesian Network.  It finds the name, type,
	  * states, and coordinates of this node.
	  *
	  * @param sbuf a string that contians all of the information for
	  * 	one node
	  */
	private void loadNodeXMLBIF(String sbuf, boolean currentVer) {
                ////System.out.println("enter loadNodeXMLBIF!");
		int startType;
		int endType;

		int startName;
		int endName;

                // need the following four variables' information to create & add current node
		String blockName;
                Vector stateList = new Vector();
                int xcoordinate;
		int ycoordinate;

		int startOutcome;
		int endOutcome;

		int startProperty;
		int endProperty;

		int startCoordinates;
		int endCoordinates;

		startType = sbuf.indexOf("TYPE=\"");
		endType = sbuf.indexOf("\">",startType);

		startName = sbuf.indexOf("<NAME>",endType);
		endName = sbuf.indexOf("</NAME>",startName);

		blockName = sbuf.substring(startName+6,endName);

		// This is the only one that can have multiple values.
		if(currentVer) {
			startOutcome = sbuf.indexOf("<OUTCOME>",endName);
			endOutcome = sbuf.indexOf("</OUTCOME>",startOutcome);
		} else {
			startOutcome = sbuf.indexOf("<VALUE>", endName);
			endOutcome = sbuf.indexOf("</VALUE>", startOutcome);
		}

		startProperty = sbuf.indexOf("<PROPERTY>",endOutcome);
		startCoordinates = sbuf.indexOf("(",startProperty);

		endCoordinates = sbuf.indexOf(",",startCoordinates);
		endProperty = sbuf.indexOf("</PROPERTY>",endCoordinates);

		xcoordinate = new Integer(sbuf.substring(startCoordinates+1,endCoordinates)).intValue();
		ycoordinate = new Integer(sbuf.substring(endCoordinates+2,endProperty-1)).intValue();

	        // load all state values into Vector stateList
		// Note:  at this point all of the values for the states are 0.0
		while(startOutcome>endName && endOutcome<startProperty) {
			if(currentVer) {
                                stateList.addElement(sbuf.substring(startOutcome+9,endOutcome).trim());
				startOutcome = sbuf.indexOf("<OUTCOME>",endOutcome);
				endOutcome = sbuf.indexOf("</OUTCOME>",startOutcome);
			} else {
                                stateList.addElement(sbuf.substring(startOutcome+9,endOutcome).trim());
                                startOutcome = sbuf.indexOf("<VALUE>", endOutcome);
				endOutcome = sbuf.indexOf("</VALUE>", startOutcome);
			}
		}

                this.addItemToBayNet(blockName,xcoordinate,ycoordinate,stateList);

	}

	/**
	  * This function looks for each seperate probability definiton
	  * and seperates them and will call a function that will parse
	  * the information out each of them.
	  *
	  * @param sbuf the string representation of the entire file
	  */
	private void loadProbabilityDistributionsXMLBIF(String sbuf, boolean currentVer) {
		int startDefinition;
		int endDefinition;

		if(currentVer) {
			startDefinition = sbuf.indexOf("<DEFINITION>");
			endDefinition = sbuf.indexOf("</DEFINITION>");
		} else {
			startDefinition = sbuf.indexOf("<PROBABILITY>");
			endDefinition = sbuf.indexOf("</PROBABILITY>");
		}

		while(startDefinition > 0) {
			loadDependenciesXMLBIF(sbuf.substring(startDefinition,endDefinition));
			if(currentVer) {
				startDefinition = sbuf.indexOf("<DEFINITION>",endDefinition);
				endDefinition = sbuf.indexOf("</DEFINITION>",startDefinition);
			} else {
				startDefinition = sbuf.indexOf("<PROBABILITY>",endDefinition);
				endDefinition = sbuf.indexOf("</PROBABILITY>",startDefinition);
			}
		}
	}

	/**
	  * This function takes in one dependency and it will located the
	  * parents and the children if there are any, and populate the
	  * probabibility table with the corresponding values.
	  *
	  * @param sbuf a string that contains only one functional dependency
	  */
	private void loadDependenciesXMLBIF(String sbuf) {
                //System.out.println("call loadDependenciesXMLBIF...");
                //System.out.println("sbuf = "+ sbuf);
               //this.addCPTtoBayNetColumnFirst(String blockName, parentList, probabilitiesList);

                String blockName;
                Vector parentList = new Vector();
                String probabilitiesList;

		int startFor;
		int endFor;

		int startGiven;
		int endGiven;

		int startTable;
		int endTable;

		startFor = sbuf.indexOf("<FOR>");
		endFor = sbuf.indexOf("</FOR>");

		blockName = sbuf.substring(startFor+5,endFor).trim();
		////System.out.println("childname = "+childname);

		// need to locate the parent
		startGiven = sbuf.indexOf("<GIVEN>",endFor);
		endGiven = sbuf.indexOf("</GIVEN>",startGiven);


		while(startGiven > startFor) {
			// need to locate the parents
			parentList.addElement(sbuf.substring(startGiven+7,endGiven).trim());
			////System.out.println("\t" + parentname);

			startGiven = sbuf.indexOf("<GIVEN>",endGiven);
			endGiven = sbuf.indexOf("</GIVEN>",startGiven);

		}

		// add the entries into the table here
		startTable = sbuf.indexOf("<TABLE>",endFor);
		endTable = sbuf.indexOf("</TABLE>",startTable);

		probabilitiesList = sbuf.substring(startTable+7,endTable).trim();
		////System.out.println(buf);
                this.addCPTtoBayNetColumnFirst(blockName, parentList, probabilitiesList, false);

	}

	/**
	  * Loads all of the nodes that are located into the file.  This
	  * function seperates all of the nodes and then calls another
	  * function that well parse each individual data stream and pull
	  * out the relevant information.
	  *
	  * @param sbuf a string of the entire file
	  */
	private void loadVariablesBIF(String sbuf) {
		int startVar;
		int endVar;
                //System.out.println("enter loadVariablesBIF ...");
                //System.out.println("sbuf = " + sbuf);
		startVar = sbuf.indexOf("variable ");
		endVar = sbuf.indexOf("}", startVar);
		endVar = sbuf.indexOf("}", endVar + 1);

		while(startVar>0) {
			loadNodeBIF(sbuf.substring(startVar, endVar));

			startVar = sbuf.indexOf("variable ", endVar);
			endVar = sbuf.indexOf("}", startVar);
			endVar = sbuf.indexOf("}", endVar + 1);
		}
	}

	/**
	   This function gets all of the information for one node
	  * and creates a new Block that is a representation of this node
	  * and adds it to the Bayesian Network.  It finds the name, type,
	  * states, and coordinates of this node.
	  *
	  * @param sbuf a string that contians all of the information for
	  * 	one node
	  */
	private void loadNodeBIF(String sbuf) {
        //System.out.println("enter loadNodeBIF ...");
                //System.out.println("sbuf = " + sbuf);
		int startName;
		int endName;

		int startOutcome;
		int endOutcome;

		int startType;
		int endType;

		int startProperty;
		int endProperty;

		int startCoordinates;
		int endCoordinates;

		int xcoordinate;
		int ycoordinate;
                Vector stateList = new Vector();
		String blockName;

		String type;

		ChanceBlock cb;
		Ellipse2D.Double e2d;
		Item item;

		startName = sbuf.indexOf("variable");
		endName = sbuf.indexOf("{",startName+1);

                // get blockName
		blockName = trimexcess(sbuf.substring(startName + 8, endName));
		////System.out.println("blockname ="+blockName);

		startType = sbuf.indexOf("type",endName);
		endType = sbuf.indexOf("[",startType);

		type = trimexcess(sbuf.substring(startType + 5, endType));

      		startOutcome = sbuf.indexOf("{", endType);
		endOutcome = sbuf.indexOf(" ", startOutcome + 4);
		startProperty = sbuf.indexOf("property",endOutcome);
		startCoordinates = sbuf.indexOf("(",startProperty);
		endCoordinates = sbuf.indexOf(",", startCoordinates);
		endProperty = sbuf.indexOf(";", endCoordinates);

                // get X, Y coordinates
		if(startProperty > 0) {
			xcoordinate = new Integer(trimexcess(sbuf.substring(startCoordinates + 1,endCoordinates))).intValue();
			ycoordinate = new Integer(trimexcess(sbuf.substring(endCoordinates, endProperty - 3))).intValue();
			startProperty = sbuf.indexOf(";");
		} else {
			startProperty = sbuf.indexOf(";");
			xcoordinate = 100;
			ycoordinate = 100;
		}
                // get stateList
		// Note:  at this point all of the values for the states are 0.0
		while(startOutcome > endName && endOutcome < startProperty && endOutcome > 0) {
                        stateList.addElement(trimexcess(sbuf.substring(startOutcome + 1, endOutcome)));
			startOutcome = sbuf.indexOf(" ", endOutcome);
			endOutcome = sbuf.indexOf(" ", startOutcome + 1);
			while(endOutcome == startOutcome + 1) {
				endOutcome = sbuf.indexOf(" ", endOutcome + 1);
			}
		}

		this.addItemToBayNet(blockName, xcoordinate, ycoordinate, stateList);
	}

	/**
	  * This function looks for each seperate probability definiton
	  * and seperates them and will call a function that will parse
	  * the information out each of them.
	  *
	  * @param sbuf the string representation of the entire file
	  */
	private void loadProbabilityDistributionsBIF(String sbuf) {
                //System.out.println("enter loadProbabilityDistributionsBIF ...");
                //System.out.println("sbuf = " + sbuf);
		int startDefinition;
		int endDefinition;

		startDefinition = sbuf.indexOf("probability (");
		endDefinition = sbuf.indexOf("}",startDefinition);

		while(startDefinition > 0) {
			loadDependenciesBIF(sbuf.substring(startDefinition,endDefinition));

			startDefinition = sbuf.indexOf("probability (",endDefinition);
			endDefinition = sbuf.indexOf("}",startDefinition);
		}
	}

	/**
	  * This function takes in one dependency and it will located the
	  * parents and the children if there are any, and populate the
	  * probabibility table with the corresponding values.
	  *
	  * @param sbuf a string that contains only one functional dependency
	  */
	private void loadDependenciesBIF(String sbuf) {
                ////System.out.println("*********enter loadDependenciesBIF ...");
                ////System.out.println("sbuf = " + sbuf);
		Item citem;
		Item pitem;
		int loc;
		int size;
		int numParents;

		int startFor;
		int endFor;

		int startGiven;
		int endGiven;

		String childname;
		String parentname;
		String tmp;

		startFor = sbuf.indexOf("(") + 1;
		endFor = sbuf.indexOf(")", startFor) - 1;

                tmp = sbuf.substring(startFor, endFor).trim();

		////System.out.println("tmp = " + tmp);

                // put all node names into nodenames, the first element is childname, all others are its parent

                String tempnode = "";
                Vector nodeNames = new Vector();
                if(tmp.indexOf("\"  \"") < 0) {
                      tempnode = trimexcess(tmp);       // this is a root node, the only node in the list
                      nodeNames.addElement(tempnode);
                }
                else {
                      while(tmp.indexOf("\"  \"") > 0) {
                           startGiven = tmp.indexOf("\"  \"");
                           tempnode = trimexcess(tmp.substring(0,startGiven));
                           nodeNames.addElement(tempnode);
                           tmp = tmp.substring(startGiven+3,tmp.length());
                           ////System.out.println("tmp = " + tmp);
                      }
                      tempnode = trimexcess(tmp);       // this is the last node in the list
                      nodeNames.addElement(tempnode);
                }

                ////System.out.println("tmp = " + tmp);
                //for(int j=0;j<nodeNames.size();j++)
                  ////System.out.println("j = " + j + " :" + nodeNames.elementAt(j).toString());

                //System.out.println("locate childname ... ");

                childname = nodeNames.elementAt(0).toString();
                citem = (Item) baynet.getFirst();
		loc = 0;
		size = baynet.size();

		while(!citem.getItem().getBlockName().equals(childname) && loc<size) {
			citem = (Item) baynet.get(loc);
			loc++;
		}

                //System.out.println("locate parents ... ");

                numParents = 0;
                for(int j=1;j<nodeNames.size();j++){
                        pitem = (Item) baynet.getFirst();
			loc = 0;
			size = baynet.size();
                        parentname = nodeNames.elementAt(j).toString();
			while(!pitem.getItem().getBlockName().equals(parentname) && loc<size) {
				pitem = (Item) baynet.get(loc);
				loc++;
			}
                        //
                        pitem.setChild(citem);
			citem.setParent(pitem);

			numParents++;

                }

                //System.out.println("numParents = " + numParents);

    		// then add the entries into the table here
		int startTable = sbuf.indexOf("table", endFor);
		int endTable = sbuf.indexOf(";", startTable);

		if(startTable < 0) {
			int startValue;
			int endValue;

			int row;
			int column;

			int startSub;
			int endSub;

			int startAttribute;
			int endAttribute;

			int startNum;
			int endNum;

			//System.out.println("Complicated BIF format");

			startValue = sbuf.indexOf("{");
			startValue = sbuf.indexOf("(", startValue);
			endValue = sbuf.indexOf(";");

			while(startValue > 0) {
				String attributes;
				String values;

				//System.out.println(sbuf.substring(startValue, endValue));
				startSub = startValue;
				endSub = sbuf.indexOf(")", startSub);

				attributes = sbuf.substring(startSub, endSub + 1);
				values = sbuf.substring(endSub + 2, endValue + 1);

				////System.out.println(attributes);
				////System.out.println(values);

				startAttribute = 0;
				if((endAttribute = attributes.indexOf(" ", startAttribute)) < 0) {
					endAttribute = attributes.indexOf(")");
				}

				int ploc;
				int npar;

				ploc = 0;
				column = 0;
				npar = citem.numParents();

				while(endAttribute > 0) {
					Item i;
					String aname;
					LinkedList ll;

					i = citem.getParent(ploc++);
					aname = trimexcess(attributes.substring(startAttribute, endAttribute));
					ll = ((ChanceBlock) i.getItem()).getAttributeNames();
					int tmpnum;
					tmpnum = 1;

					//System.out.println("Ploc = " + ploc);
					for(int j=ploc;j<citem.numParents();j++) {
						tmpnum *= ((ChanceBlock) citem.getParent(j).getItem()).numAttributes();
						//System.out.println("Name:  " + citem.getParent(j).getItem().getBlockName() + "\ttmpnum:  " + tmpnum);
					}

					//tmpnum = ll.indexOf(aname) * ((ChanceBlock) i.getItem()).numAttributes();
					tmpnum *= ll.indexOf(aname);

					//System.out.println("tmpnum = " + tmpnum);

					if(ploc != npar) {
						column += tmpnum;
					} else {
						column += ll.indexOf(aname);
					}



					//System.out.println(aname + " " + (ll.indexOf(aname)));
					//System.out.println(column);

					startAttribute = endAttribute;
					if((endAttribute = attributes.indexOf(" ", startAttribute + 1)) < 0) {
						endAttribute = attributes.indexOf(")");
					}

					if(startAttribute == endAttribute) {
						endAttribute = -1;
					}
				}

				// do the number retrieving here
				startNum = 0;
				if((endNum = values.indexOf(" ", startNum)) < 0) {
					endNum = values.indexOf(";");
				}

				double d;

				//System.out.println(citem.getItem().getBlockName());
				for(row=0;row<((ChanceBlock) citem.getItem()).numAttributes();row++) {
					d = (new Double(trimexcess(values.substring(startNum, endNum)))).doubleValue();

					startNum = endNum;
					if((endNum = values.indexOf(" ", startNum + 1)) < 0) {
						endNum = values.indexOf(";");
					}

					//((ChanceBlock) citem.getItem()).setValue(d, row, column - 1);
					((ChanceBlock) citem.getItem()).setValue(d, row, column);
					////System.out.println("(" + row + ", " + (column - 1) + ") " + d);
					//System.out.println("(" + row + ", " + column + ") " + d);
				}

				startValue = sbuf.indexOf("(", endValue);
				endValue = sbuf.indexOf(";", startValue);
			}

			//System.out.println();

		} else {
			String buf = sbuf.substring(startTable + 6, endTable);

			int startValue = 0;
			int endValue =  buf.indexOf(" ");

			if(endValue < 0 || endValue == startValue) {
				endValue = buf.indexOf(" <");
			}

			int column = 1;

			for(int i=0;i<citem.numParents();i++) {
				column *= ((ChanceBlock) citem.getParent(i).getItem()).numAttributes();
			}

			for(int j=0;j<((ChanceBlock) citem.getItem()).numAttributes();j++) {
				for(int k=0;k<column;k++) {
					double d;
					d = (new Double(trimexcess(buf.substring(startValue, endValue)))).doubleValue();

					startValue = ++endValue;
					endValue = buf.indexOf(" ", startValue);

					while(endValue == startValue) {
						endValue = buf.indexOf(" ", endValue + 1);
					}

					if(endValue < 0) {
						if(startValue < buf.length()) {
							if(buf.substring(startValue).length() > 0) {
								endValue = startValue + buf.substring(startValue).length();
							} else {
								endValue = buf.indexOf("<", startValue);
							}
						} else {
							endValue = buf.indexOf("<", startValue);
						}
					}

					if(endValue == startValue) {
						endValue = -1;
					}

					((ChanceBlock) citem.getItem()).setValue(d, j, k);
				}
			}
		}


	}

	/**
	  * Sets the current type of file that is being used.  This is useful
	  * when a file is being imported to the editor.
	  *
	  * @param type the type of the file
	  */
	public void setFileType(String type) {
		currenttype = type;
	}

	/**
	  * Returns the current type of file that is being used to save
	  * the Bayesian network status
	  *
	  * @return currenttype - the current type of the file
	  */
	public String getFileType() {
		return currenttype;
	}


        /**
         * clear comments section in .dsl format files, because it may include "node " which is used as seperator to load dsl files
         *
         * @return sbuf - the new sbuf with all comments being cleaned
         */

        private String clearDSLcomments(String sbuf, String strRemoved) {

                String tmp = sbuf;
                String result = " ";
                int startComments;
                int endComments;

                //System.out.println("clean comments...");

                while ((startComments = tmp.indexOf(strRemoved))>0) {
                  result = result + tmp.substring(0,startComments);
                  endComments = tmp.indexOf(";", startComments)+1;
                  tmp = tmp.substring(endComments,tmp.length());
                }
                result = result + tmp;
                return result;
        }


        private String cleanDNETheader(String sbuf) {

                int startComments, endComments;
                startComments = sbuf.indexOf("comment =");
                endComments = sbuf.indexOf(";", startComments);
                sbuf = sbuf.substring(endComments).trim();

                int startVisual, endVisual;
                startVisual = sbuf.indexOf("visual ");
                if(sbuf.indexOf("define node")>0)
                   endVisual = sbuf.indexOf("define node ");
                else endVisual = sbuf.indexOf("node ");
                sbuf = sbuf.substring(endVisual).trim();

                return sbuf;
        }

        private String cleanNETcomments(String sbuf) {
                String tmp = sbuf;
                String result = " ";
                for(int i=0;i<sbuf.length();i++) {
                  if(Character.isDigit(sbuf.charAt(i))) {
                    result = result + String.valueOf(sbuf.charAt(i));
                  }
                  else if(String.valueOf(sbuf.charAt(i)).equals(".")) {
                    result = result + String.valueOf(sbuf.charAt(i));
                  }
                  else if(!result.endsWith(" "))
                  {
                    result = result + " ";
                  }
                }

                return result;
        }

        private String getNETinfo(String sbuf, String tmp, char endSeperator) {
                String result;
                int startInfo = sbuf.indexOf(tmp);
                if(startInfo<0)
                  { return "" ;}
                else
                  startInfo = startInfo + tmp.length();

                int endInfo = sbuf.indexOf(endSeperator, startInfo);
                result = this.trimexcess(sbuf.substring(startInfo, endInfo).trim());
                //result = this.trimexcess(sbuf.substring(startInfo, endInfo));
                ////System.out.println("result = " + result);
                return result;
        }

        private String getDSLinfo(String sbuf, String tmp) {
                String result;
                int startInfo = sbuf.indexOf(tmp);
                if(startInfo<0)
                  { return "" ;}
                else
                  startInfo = startInfo + tmp.length();

                int endInfo = sbuf.indexOf(";", startInfo);
                result = this.trimexcess(sbuf.substring(startInfo, endInfo).trim());
                //result = this.trimexcess(sbuf.substring(startInfo, endInfo));
                ////System.out.println("result = " + result);
                return result;
        }

        private String replaceTwoSpacesToOne(String sbuf) {
                sbuf = sbuf.trim();
                String tmp = "";
                while(sbuf.indexOf("  ")>0) {
                    tmp = tmp + " " + sbuf.substring(0,sbuf.indexOf("  "));
                    sbuf = sbuf.substring(sbuf.indexOf("  ")+2).trim();
                }
                tmp = tmp +" " +sbuf;
                return tmp.trim();
        }
	private String trimexcess(String sbuf) {
		sbuf = sbuf.replace('"', ' ');
		sbuf = sbuf.replace('|', ' ');
		sbuf = sbuf.replace(',', ' ');
		sbuf = sbuf.replace('(', ' ');
                sbuf = sbuf.replace(')',' ');
                sbuf = sbuf.replace('=',' ');

		return sbuf.trim();
	}

        /**
         * This function create the item, and add it into baynet
         */
        private void addItemToBayNet(String blockName, int xcoordinate, int ycoordinate, Vector stateList) {

                ChanceBlock cb;
		Ellipse2D.Double e2d;
		Item item;
                // Creating the Node and it's corresponding Graphical
		// representation and inserting it into the block.
		cb = new ChanceBlock(blockName, xcoordinate + width / 2, ycoordinate + height / 2, false);
		e2d = new Ellipse2D.Double(xcoordinate, ycoordinate, width, height);
		item = new Item(cb,e2d);
                //System.out.println("add node \"" + blockName+"\"");
                item.print();
		baynet.add(item);

                // Done with inserting the node into the LinkedList
		// Add the states to the Node that is currently being delt with
		// Note:  at this point all of the values for the states are 0.0
                for(int i=0;i<stateList.size();i++) {
                    cb.add(stateList.elementAt(i).toString().trim());
                }
                /*
                StringTokenizer t1 = new StringTokenizer(stateList," ");
                int count = t1.countTokens();
		                    ////System.out.println("t1.countTokens():"+count);
                for(int j=0;j<count;j++)
                   cb.add(t1.nextToken().trim());
                   */


                LinkedList l = cb.getAttributeNames();
		for(int i=0;i<cb.numAttributes();i++) {
			//System.out.println(l.get(i));
		}
		//System.out.println();
        }

        /**
         * This function set up the child-parent relationship and fill up CPTs
         * If columnFirst==true, it fills the CPT column by column, i.e., column first.
         * If columnFirst==false, it fills the CPT row by row, i.e., row first.(for example in xmlbif format)
         *
         */
        private void addCPTtoBayNetColumnFirst(String blockName, Vector parentList, String probabilitiesList, boolean columnFirst) {

                // ******Now set up parent-child relationship
                //System.out.println("blockName = \"" + blockName+"\"");
                //System.out.println("parentList = " + parentList);
                //System.out.println("probabilitiesList = " + probabilitiesList);


                Item citem;
                Item pitem;
                int loc;


                String parentname = "";
                String childname = blockName;
                // first locate child(citem)
                citem = (Item) baynet.getFirst();
		loc = 0;
		int size = baynet.size();

		while(!citem.getItem().getBlockName().equals(childname) && loc<size) {
			citem = (Item) baynet.get(loc);
			loc++;
		}

                // then locate parents...
                for(int j=0;j<parentList.size();j++) {
                  parentname = parentList.elementAt(j).toString().trim();
                  ////System.out.println("parentname = " + parentname);
                  pitem = (Item) baynet.getFirst();
                  loc = 0;
		  size = baynet.size();
                  while(!pitem.getItem().getBlockName().equals(parentname) && loc<size) {
				pitem = (Item) baynet.get(loc);
				loc++;
			}

                  pitem.setChild(citem);
                  citem.setParent(pitem);
                }
                // add the entries into the table here
                // first store these entries into a Vector: probabilitiesTable

		Vector probabilitiesTable = new Vector();
                StringTokenizer t3 = new StringTokenizer(probabilitiesList," ");
                int count = t3.countTokens();
                double temp_d = 0;;
                boolean hasLargeProbNum = false;
                for(int j=0;j<count;j++) {
                    probabilitiesTable.addElement(t3.nextToken().trim());
                    //System.out.println("probabilitiesTable.lastElement().toString() = " + probabilitiesTable.lastElement().toString());
                    temp_d = Double.parseDouble(probabilitiesTable.lastElement().toString());
                    if(temp_d >1)
                      hasLargeProbNum = true;
                }


                //System.out.println("probabilitiesTable = " + probabilitiesTable.toString());


                int column = 1;
                for(int i=0;i<citem.numParents();i++) {
			column *= ((ChanceBlock) citem.getParent(i).getItem()).numAttributes();
		}
                //System.out.println("column = " + column);
                //int row =
                //System.out.println("row = " + ((ChanceBlock) citem.getItem()).numAttributes());
                int indexProbTable = 0;
                if(columnFirst==true) {
                  for(int k=0;k<column;k++) {
                       for(int j=0;j<((ChanceBlock) citem.getItem()).numAttributes();j++) {
				double d;

				try {  // //System.out.println("indexProbTable = " + indexProbTable);
                                        ////System.out.println(probabilitiesTable.elementAt(indexProbTable).toString().trim());
					d = (new Double(probabilitiesTable.elementAt(indexProbTable).toString().trim()).doubleValue());
                                        if(hasLargeProbNum)
                                           d = d/100;      // some files use large numbers, for example, (5 90 5) instead of (0.05, 0.9, 0.05)
				} catch(StringIndexOutOfBoundsException sioobe) {
					d = 0.0;
				}
                                indexProbTable++;
				((ChanceBlock) citem.getItem()).setValue(d, j, k);
			}
		  }
                } else if (columnFirst==false) {

                       for(int j=0;j<((ChanceBlock) citem.getItem()).numAttributes();j++) {
                         for(int k=0;k<column;k++) {
				double d;

				try {
                                        //System.out.println(probabilitiesTable.elementAt(indexProbTable).toString().trim());
					d = (new Double(probabilitiesTable.elementAt(indexProbTable).toString().trim()).doubleValue());
				} catch(StringIndexOutOfBoundsException sioobe) {
					d = 0.0;
				}
                                indexProbTable++;
				((ChanceBlock) citem.getItem()).setValue(d, j, k);
			}
		  }
                }

        }
}
