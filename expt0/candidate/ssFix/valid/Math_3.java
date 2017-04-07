package edu.psu.geovista.datamining.entropy;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

import java.util.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

import edu.psu.geovista.datamining.util.*;
import edu.psu.geovista.ui.event.*;
import edu.psu.geovista.symbolization.*;
import edu.psu.geovista.datamining.data.*;
import java.text.*;

public class EntropyMapUI
    extends JPanel
    implements MouseListener,
    MouseMotionListener,
    KeyListener,
    ColorArrayListener,
    SelectionListener,
    IndicationListener {

  private static Random rand = new Random();

  private DataSetByInstances data;
  private double[] locX;
  private double[] locY;
  private double[] attA; //may be normalized in various ways
  private double[] attB; //may be normalized in various ways
  private double[] permAttA;
  private double[] permAttB;
  private double[] originalAttA; //keep the original values, which may be normalized using max-min or stdev-mean
  private double[] originalAttB; //keep the original values, which may be normalized using max-min or stdev-mean

  private int[] samplesize = new int[] {50}; //{25, 35, 45, 55, 75};
  private double[][] entropy = new double[samplesize.length][];
  private int[][][] kNN;
  private String folder = "C:/_DATA/LocalEntropy/entropyresults/";
  private int permutations = 2000;

  //load centroid data first (click no for ordering)
  //then load the normal polygon (county) data
  //then click calculate entropy button.
  public void setDataSet(DataSetByInstances data) {
    double permutationValues[] = null;
    NumberFormat nf = NumberFormat.getInstance();
    if (nf instanceof DecimalFormat)
      ((DecimalFormat)nf).applyPattern("#####0.0000");

    if (this.data != data) {
      this.data = data;
      int datasize = data.getInstSize();
      this.locX = new double[datasize];
      this.locY = new double[datasize];
      this.attA = new double[datasize];
      this.attB = new double[datasize];
      this.permAttA = new double[datasize];
      this.permAttB = new double[datasize];
      this.originalAttA = new double[datasize];
      this.originalAttB = new double[datasize];
      String[] names = data.getAttNames();
      this.kNN = new int[samplesize.length][datasize][];
      System.out.println(names[0] + ", " + names[1]);
      for (int i = 0; i < datasize; i++) {
        locX[i] = data.getInstance(i).getSpatialLocation().getX();
        locY[i] = data.getInstance(i).getSpatialLocation().getY();
        attA[i] = data.getInstance(i).getElement(0);
        attB[i] = data.getInstance(i).getElement(1);
        originalAttA[i] = attA[i];
        originalAttB[i] = attB[i];
      }

      //Normalize the data with Nested Means
      this.normalizeWithNM(attA, attB);

      int[] permIds = new int[attA.length];
      DataGenerator.generateRandomIntegersWithoutReplacement(permIds, rand, 0, permIds.length, permIds.length);
      for (int i=0; i< permIds.length; i++){
        this.permAttA[i] = this.attA[permIds[i]];
        this.permAttB[i] = this.attB[permIds[i]];
      }
      //this.normalizeWithMinMaxWithSpecialProcessingOfOutliers(attA, attB);

      //Construct a color scheme
      Color[] cs = new Color[9]; //makeColors(percentiles.length + 1, Color.red, Color.white,Color.blue);
      cs[0] = new Color(255, 0, 0);
      cs[1] = new Color(255, 119, 0);
      cs[2] = new Color(255, 200, 0);
      cs[3] = new Color(238, 255, 56);
      cs[4] = new Color(255, 255, 255);
      cs[5] = new Color(89, 255, 255);
      cs[6] = new Color(51, 194, 255);
      cs[7] = new Color(56, 106, 255);
      cs[8] = new Color(0, 0, 255);

      double[] breaks = null;

      //Calcualte data standard deviation
      double dataStdDevA = MathUtil.getStdDev(attA);
      double dataStdDevB = MathUtil.getStdDev(attB);
      System.out.println("global stddev A=" + nf.format(dataStdDevA) + ", B=" +
                         nf.format(dataStdDevB));

      //Returns the all the permutation replications
      //Outlier percentage--i.e., % of points treated as outlier(s)
      int[] percentageOutliers = new int[] {5}; //{5, 10, 15}
      //samplesize = new int[] {50}; //{25, 35, 45, 55, 75};

      //Weighted edges
      float edgepower = 1.0f;

      int numOutliers;
      double[][] localR2 = new double[samplesize.length][datasize];
      String title;
      for (int k = 0; k < percentageOutliers.length; k++) {
        for (int s = 0; s < samplesize.length; s++) {
          numOutliers = (int) ( (float) percentageOutliers[k] / 100.0f *
                               samplesize[s] + 0.5);
         title = "[KNN = " + samplesize[s] + "] [Outlier% =" +
             percentageOutliers[k] + "%, i.e., " + numOutliers +
             " edges removed]";
         System.out.println(title);

         //This method permutes an input data array without considering geographic locations
         //and then return an array of entropy values for each random sampling.
         permutationValues = TestRenyiEntropy.calculatePermutationEntroy(
              rand,
              attA,
              attB,
              permutations,
              samplesize[s],
              edgepower,
              numOutliers);

          System.out.println("--Non-spatial permutation--");
          breaks = makeMixedBreaks(permutationValues);
          for (int i = 0; i < breaks.length; i++)
            System.out.print("\t"+ nf.format(breaks[i]));
          System.out.println("--------------------");

          DistributionHistogram hist = new DistributionHistogram(
              "Permutation Entropy Values",
              permutationValues,
              breaks,
              new String[] {"-5*StdDev", "-3*StdDev", "p<0.01", "p<0.05",
              "p<0.05", "p<0.01","+3*StdDev", "+5*StdDev"});

          JFrame f = new JFrame("Permutation Distribution " + title);
          f.getContentPane().setLayout(new GridLayout(2,1));
          f.getContentPane().add(hist);
          f.setSize(600, 900);
          f.setVisible(true);
          f.repaint();

          //Permutation over the geographic space, then calculate local entropies.
//          double[] spaPermEntropies = TestRenyiEntropy.searchLocalPatternKNInstanceByInstance(
//              permAttA,
//              permAttB,
//              locX,
//              locY,
//              samplesize[s],
//              edgepower,
//              numOutliers,
//              false,
//              kNN[s],
//              dataStdDevA,
//              dataStdDevB);
//          double[] spaPermBreaks = makeMixedBreaks(spaPermEntropies);
//
//          DistributionHistogram hist2 = new DistributionHistogram(
//              "Spatial Permutation Entropy Values",
//              spaPermEntropies,
//              spaPermBreaks,
//              new String[] {"-5*StdDev", "-3*StdDev", "p<0.01", "p<0.05",
//              "p<0.05", "p<0.01", "+3*StdDev", "+5*StdDev"});
//          f.getContentPane().add(hist2);

          //Search local patterns for different neighborhood sizes
          entropy[s] = TestRenyiEntropy.searchLocalPatternKNInstanceByInstance(
              attA,
              attB,
              locX,
              locY,
              samplesize[s],
              edgepower,
              numOutliers,
              false,
              kNN[s],
              dataStdDevA,
              dataStdDevB);

          DistributionHistogram hist3 = new DistributionHistogram(
              "Local Entropy Values",
              entropy[s],
              breaks,
              null);
          f.getContentPane().add(hist3);

          localR2[s] = LocalLinearRegression.
              localLLineareRegressionKNInstanceByInstance(
                  this.originalAttA,
                  this.originalAttB,
                  numOutliers,
                  kNN[s]);
        }
        saveEntropyToFile(entropy,
                          "outliers%_" + percentageOutliers[k] +
                          "_entropy_data_local");
        saveEntropyToFile(localR2,
                          "outliers%_" + percentageOutliers[k] +
                          "_R2_local");
      }
      //Make a choropleth map, using the last sampe size, the last percentage of outliers, and the last type of break values.
      prepareColorArrays(entropy[samplesize.length - 1], breaks, cs);
    }
  }

  /***
   *  Normalize the data with Nested Means. Data falling in the two end bins are processed differently
   * to remove the impact of extreme values.
   */
  private void normalizeWithNM(double[] A, double[] B) {
    double[] tempA = new double[A.length];
    double[] tempB = new double[B.length];
    System.arraycopy(A, 0, tempA, 0, A.length);
    System.arraycopy(B, 0, tempB, 0, B.length);
    Arrays.sort(tempA);
    Arrays.sort(tempB);

    //Normalize again with nested means
    NestedMean nmX = new NestedMean(tempA, 0, tempA.length - 1, 0, 5);
    NestedMean nmY = new NestedMean(tempB, 0, tempB.length - 1, 0, 5);
    double[] xmean = nmX.getMeans(4);
    double[] ymean = nmY.getMeans(4);
    double xmin = tempA[0];
    double xmax = tempA[tempA.length - 1];
    double ymin = tempB[0];
    double ymax = tempB[tempB.length - 1];

    SortableObject[] smallX, largeX, smallY, largeY;
    smallX = new SortableObject[A.length];
    largeX = new SortableObject[A.length];
    smallY = new SortableObject[A.length];
    largeY = new SortableObject[A.length];
    int countSX=0, countLX=0, countSY=0, countLY=0;
    for (int i = 0; i < A.length; i++) {
      if (A[i] < xmean[0]){
        smallX[countSX] = new SortableObject(i, A[i]);
        countSX++;
      }
      else if (A[i] > xmean[ymean.length -1]){
        largeX[countLX] = new SortableObject(i, A[i]);
        countLX++;
      }
      if (B[i] < ymean[0]){
        smallY[countSY] = new SortableObject(i, B[i]);
        countSY++;
      }
      else if (B[i] > ymean[ymean.length -1]){
        largeY[countLY] = new SortableObject(i, B[i]);
        countLY++;
      }
    }
    Arrays.sort(smallX, 0, countSX);
    Arrays.sort(largeX, 0, countLX);
    Arrays.sort(smallY, 0, countSY);
    Arrays.sort(largeY, 0, countLY);

  //=============================BEGIN of processing end bins (which may contain outliers).
    //For the bins at two ends, which might contain outliers, following steps are
    //followed to avoid the impact of outliers.
    //if the distance between two neighboring values > average neighbor distance in that bin
    //shrink that distance to the average distance.
    double len = xmean[0] - smallX[countSX-1].getValue();
    double averageLen = (xmean[0] - xmin)/countSX;
    double preValue = xmean[0];
    for (int i = countSX-1; i >= 0; i--) {
      if (len > averageLen) {
        A[smallX[i].getId()] = preValue - averageLen;
      }else
        A[smallX[i].getId()] = preValue - len;
      if (i>0)
        len = smallX[i].getValue() - smallX[i-1].getValue();
      preValue = A[smallX[i].getId()];
    }
    xmin = preValue;

    len = ymean[0] - smallY[countSY-1].getValue();
    averageLen = (ymean[0] - ymin)/countSY;
    preValue = ymean[0];
    for (int i = countSY-1; i >= 0; i--) {
      if (len > averageLen) {
        B[smallY[i].getId()] = preValue - averageLen;
      }else
        B[smallY[i].getId()] = preValue - len;
      if (i>0)
        len = smallY[i].getValue() - smallY[i-1].getValue();
      preValue = B[smallY[i].getId()];
    }
    ymin = preValue;

    len = largeX[0].getValue() - xmean[xmean.length -1];
    averageLen = (xmax - xmean[xmean.length -1]) / countLX;
    preValue =  xmean[xmean.length -1];
    for (int i = 0; i < countLX; i++) {
      if (len > averageLen) {
        A[largeX[i].getId()] = preValue + averageLen;
      }
      else
        A[largeX[i].getId()] = preValue + len;
      if (i < countLX -1)
        len = largeX[i+1].getValue() - largeX[i].getValue();
      preValue = A[largeX[i].getId()];
    }
    xmax = preValue;

    len = largeY[0].getValue() - ymean[ymean.length -1];
    averageLen = (ymax - ymean[ymean.length -1]) / countLY;
    preValue =  ymean[ymean.length -1];
    for (int i = 0; i < countLY; i++) {
      if (len > averageLen) {
        B[largeY[i].getId()] = preValue + averageLen;
      }
      else
        B[largeY[i].getId()] = preValue + len;
      if (i < countLY -1)
        len = largeY[i+1].getValue() - largeY[i].getValue();
      preValue = B[largeY[i].getId()];
    }
    ymax = preValue;
//=============================END of processing end bins (which may contain outliers).

    System.out.println("\nData are normalized using "+ xmean.length +" nested means.");
    for (int i = 0; i < A.length; i++) {
      int b;
      for (b = 0; b < xmean.length; b++) {
        if (A[i] < xmean[b]) {
          if (b == 0){ //the smallest cell
            A[i] = (A[i] - xmin) / (xmean[0] - xmin);
          }
          else{ //b>=1
            A[i] = b + (A[i] - xmean[b - 1]) / (xmean[b] - xmean[b - 1]);
          }
          break;
        }
      }
      if (b == xmean.length) { //belong to the largest group
        A[i] = (A[i] - xmean[b - 1]) / (xmax - xmean[b - 1]) + b;
      }
      A[i] = A[i] / (float)(xmean.length + 1);
    }
    for (int i = 0; i < B.length; i++) {
      int b;
      for (b = 0; b < ymean.length; b++) {
        if (B[i] < ymean[b]) {
          if (b == 0) //the smallest cell
            B[i] = (B[i] - ymin) / (ymean[0] - ymin);
          else //b>=1
            B[i] = (B[i] - ymean[b - 1]) / (ymean[b] - ymean[b - 1]) +
                b;
          break;
        }
      }
      if (b == ymean.length) { //belong to the largest group
        B[i] = (B[i] - ymean[b - 1])/( ymax - ymean[b - 1]) + b;
      }
      B[i] = B[i] / (float)(ymean.length + 1);
    }
    //END of normalization with nested means
  }

  /***
   *  Normalize the data with Min-Max. Data falling in the two end bins (5%) are processed differently
   * to remove the impact of extreme values.
   */
  private void normalizeWithMinMaxWithSpecialProcessingOfOutliers(double[] A, double[] B) {
    double[] tempA = new double[A.length];
    double[] tempB = new double[B.length];
    System.arraycopy(A, 0, tempA, 0, A.length);
    System.arraycopy(B, 0, tempB, 0, B.length);
    Arrays.sort(tempA);
    Arrays.sort(tempB);

    //Normalize again with nested means
    double xmin = tempA[0];
    double xmax = tempA[tempA.length - 1];
    double ymin = tempB[0];
    double ymax = tempB[tempB.length - 1];

    int percent5length = (int)(A.length * 0.05 + 0.5);
    double sxOutlierCut = tempA[percent5length];
    double lxOutlierCut = tempA[A.length - percent5length -1];
    double syOutlierCut = tempB[percent5length];
    double lyOutlierCut = tempB[B.length - percent5length -1];

    SortableObject[] smallX, largeX, smallY, largeY;
    smallX = new SortableObject[A.length];
    largeX = new SortableObject[A.length];
    smallY = new SortableObject[A.length];
    largeY = new SortableObject[A.length];
    int countSX=0, countLX=0, countSY=0, countLY=0;
    for (int i = 0; i < A.length; i++) {
      if (A[i] < sxOutlierCut){
        smallX[countSX] = new SortableObject(i, A[i]);
        countSX++;
      }
      else if (A[i] > lxOutlierCut){
        largeX[countLX] = new SortableObject(i, A[i]);
        countLX++;
      }
      if (B[i] < syOutlierCut){
        smallY[countSY] = new SortableObject(i, B[i]);
        countSY++;
      }
      else if (B[i] > lyOutlierCut){
        largeY[countLY] = new SortableObject(i, B[i]);
        countLY++;
      }
    }
    Arrays.sort(smallX, 0, countSX);
    Arrays.sort(largeX, 0, countLX);
    Arrays.sort(smallY, 0, countSY);
    Arrays.sort(largeY, 0, countLY);

  //=============================BEGIN of processing end bins (which may contain outliers).
    //For the bins at two ends, which might contain outliers, following steps are
    //followed to avoid the impact of outliers.
    //if the distance between two neighboring values > average neighbor distance in that bin
    //shrink that distance to the average distance.
    double len = sxOutlierCut - smallX[countSX-1].getValue();
    double averageLen = (sxOutlierCut - xmin)/countSX;
    double preValue = sxOutlierCut;
    for (int i = countSX-1; i >= 0; i--) {
      if (len > averageLen) {
        A[smallX[i].getId()] = preValue - averageLen;
      }else
        A[smallX[i].getId()] = preValue - len;
      if (i>0)
        len = smallX[i].getValue() - smallX[i-1].getValue();
      preValue = A[smallX[i].getId()];
    }
    xmin = preValue;

    len = syOutlierCut - smallY[countSY-1].getValue();
    averageLen = (syOutlierCut - ymin)/countSY;
    preValue = syOutlierCut;
    for (int i = countSY-1; i >= 0; i--) {
      if (len > averageLen) {
        B[smallY[i].getId()] = preValue - averageLen;
      }else
        B[smallY[i].getId()] = preValue - len;
      if (i>0)
        len = smallY[i].getValue() - smallY[i-1].getValue();
      preValue = B[smallY[i].getId()];
    }
    ymin = preValue;

    len = largeX[0].getValue() - lxOutlierCut;
    averageLen = (xmax - lxOutlierCut) / countLX;
    preValue =  lxOutlierCut;
    for (int i = 0; i < countLX; i++) {
      if (len > averageLen) {
        A[largeX[i].getId()] = preValue + averageLen;
      }
      else
        A[largeX[i].getId()] = preValue + len;
      if (i < countLX -1)
        len = largeX[i+1].getValue() - largeX[i].getValue();
      preValue = A[largeX[i].getId()];
    }
    xmax = preValue;

    len = largeY[0].getValue() - lyOutlierCut;
    averageLen = (ymax - lxOutlierCut) / countLY;
    preValue =  lxOutlierCut;
    for (int i = 0; i < countLY; i++) {
      if (len > averageLen) {
        B[largeY[i].getId()] = preValue + averageLen;
      }
      else
        B[largeY[i].getId()] = preValue + len;
      if (i < countLY -1)
        len = largeY[i+1].getValue() - largeY[i].getValue();
      preValue = B[largeY[i].getId()];
    }
    ymax = preValue;
//=============================END of processing end bins (which may contain outliers).

    System.out.println("\nData are normalized using Min-Max.");
    for (int i = 0; i < A.length; i++) {
        A[i] = (A[i] - xmin) / (xmax - xmin);
        B[i] = (B[i] - ymin) / (ymax - ymin);
    }
    //END of normalization with nested means
  }

  /***
   *  Normalization with quantiles. outliers are given a new value equal to (Q2A - (Q3A - Q1A)*1.5f)
   */
  private void normalizeByTransformingOutliers(double[] A, double[] B) {
    System.out.println("\nData are normalized using quantiles and \n" +
                       "outliers [beyond the range (Q2A - (Q3A - Q1A)*1.5f) on both sides of the median] \n" +
                       "are squeazed into an area of range (Q3A - Q1A)*1.5f");
    double[] tempA = new double[A.length];
    double[] tempB = new double[B.length];
    System.arraycopy(A, 0, tempA, 0, A.length);
    System.arraycopy(B, 0, tempB, 0, B.length);
    Arrays.sort(tempA);
    Arrays.sort(tempB);

    double Q1A, Q2A, Q3A, minA, maxA;
    double Q1B, Q2B, Q3B, minB, maxB;
    double factor = 1.5f;
    double outlierPer = 0.025; //5%
    double minOutlierMarkA, maxOutlierMarkA;
    double minOutlierMarkB, maxOutlierMarkB;
    Q2A = (tempA[tempA.length / 2] + tempA[ (tempA.length - 1) / 2]) / 2.0f;
    Q1A = (tempA[tempA.length / 4] + tempA[ (tempA.length - 1) / 4]) / 2.0f;
    Q3A = (tempA[tempA.length - 1 - (tempA.length / 4)] + tempA[tempA.length -
           1 - ( (tempA.length - 1) / 4)]) / 2.0f;
    Q2B = (tempB[tempB.length / 2] + tempB[ (tempB.length - 1) / 2]) / 2.0f;
    Q1B = (tempB[tempB.length / 4] + tempB[ (tempB.length - 1) / 4]) / 2.0f;
    Q3B = (tempB[tempB.length - 1 - (tempB.length / 4)] + tempB[tempB.length -
           1 - ( (tempB.length - 1) / 4)]) / 2.0f;

//    minOutlierMarkA = (tempA[(int)(tempA.length * outlierPer)] + tempA[(int)((tempA.length-1) * outlierPer)]) / 2.0f;
//    maxOutlierMarkA = (tempA[tempA.length-1 -(int)(tempA.length * outlierPer)] + tempA[tempA.length-1 - (int)((tempA.length-1) * outlierPer)]) / 2.0f;
//    minOutlierMarkB = (tempB[(int)(tempB.length * outlierPer)] + tempB[(int)((tempB.length-1) * outlierPer)]) / 2.0f;
//    maxOutlierMarkB = (tempB[tempB.length-1 -(int)(tempB.length * outlierPer)] + tempB[tempB.length-1 - (int)((tempB.length-1) * outlierPer)]) / 2.0f;
    minOutlierMarkA = (Q2A - (Q3A - Q1A) * factor);
    maxOutlierMarkA = (Q2A + (Q3A - Q1A) * factor);
    minOutlierMarkB = (Q2B - (Q3B - Q1B) * factor);
    maxOutlierMarkB = (Q2B + (Q3B - Q1B) * factor);

    minA = tempA[0];
    minB = tempB[0];
    maxA = tempA[tempA.length - 1];
    maxB = tempB[tempB.length - 1];
    if (minA < minOutlierMarkA)
      minA = minOutlierMarkA;
    if (maxA > maxOutlierMarkA)
      maxA = maxOutlierMarkA;
    if (minB < minOutlierMarkB)
      minB = minOutlierMarkB;
    if (maxB > maxOutlierMarkB)
      maxB = maxOutlierMarkB;

    double rangeA = (maxA - minA);
    double rangeB = (maxB - minB);
    for (int i = 0; i < A.length; i++) {
      if (A[i] < minA)
        A[i] = minA;
      else if (A[i] > maxA)
        A[i] = maxA;
      A[i] = (A[i] - minA) / rangeA;
    }
    for (int i = 0; i < B.length; i++) {
      if (B[i] < minB)
        B[i] = minB;
      else if (B[i] > maxB)
        B[i] = maxB;
      B[i] = (B[i] - minB) / rangeB;
    }
  }

  /***
   *  Normalization with quantiles. outliers are given a new value equal to (Q2A - (Q3A - Q1A)*1.5f)
   */
  private void normalizeWithQuantiles(double[] A, double[] B) {
    System.out.println("\nData are normalized using quantiles\n");
    double[] tempA = new double[A.length];
    double[] tempB = new double[B.length];
    System.arraycopy(A, 0, tempA, 0, A.length);
    System.arraycopy(B, 0, tempB, 0, B.length);
    Arrays.sort(tempA);
    Arrays.sort(tempB);

    //Normalize again with nested means
    double[] xquantiles = new double[16];
    double[] yquantiles = new double[16];
    double xmin = tempA[0];
    double xmax = tempA[tempA.length - 1];
    double ymin = tempB[0];
    double ymax = tempB[tempB.length - 1];
    for (int i=1; i< xquantiles.length; i++){
      xquantiles[i-1] = tempA[(int)(tempA.length* i / 16.0f)];
      yquantiles[i-1] = tempB[(int)(tempB.length* i / 16.0f)];
    }
    xquantiles[xquantiles.length -1] = xmax + 1;
    yquantiles[yquantiles.length -1] = ymax + 1;

    for (int i = 0; i < A.length; i++) {
      int b;
      for (b = 0; b < xquantiles.length; b++) {
        if (A[i] < xquantiles[b]) {
          if (b == 0) //the smallest cell
            A[i] = (A[i] - xmin) / (xquantiles[0] - xmin);
          else //b>=1
            A[i] = (A[i] - xquantiles[b - 1]) / (xquantiles[b] - xquantiles[b - 1]) +
                b;
          break;
        }
      }
    }
    for (int i = 0; i < B.length; i++) {
      int b;
      for (b = 0; b < yquantiles.length; b++) {
        if (B[i] < yquantiles[b]) {
          if (b == 0) //the smallest cell
            B[i] = (B[i] - ymin) / (yquantiles[0] - ymin);
          else //b>=1
            B[i] = (B[i] - yquantiles[b - 1]) / (yquantiles[b] - yquantiles[b - 1]) +
                b;
          break;
        }
      }
    }
  }

  /***
   *  Normalization with quantiles. outliers are given a new value equal to (Q2A - (Q3A - Q1A)*1.5f)
   */
  private void normalizeWithMinMax(double[] A, double[] B) {
    System.out.println("\nData are normalized using Min/Max \n");
    double[] tempA = new double[A.length];
    double[] tempB = new double[B.length];
    System.arraycopy(A, 0, tempA, 0, A.length);
    System.arraycopy(B, 0, tempB, 0, B.length);
    Arrays.sort(tempA);
    Arrays.sort(tempB);
    double minA = tempA[0];
    double minB = tempB[0];
    double maxA = tempA[tempA.length - 1];
    double maxB = tempB[tempB.length - 1];

    double rangeA = (maxA - minA);
    double rangeB = (maxB - minB);
    for (int i = 0; i < A.length; i++) {
      if (A[i] < minA)
        A[i] = minA;
      else if (A[i] > maxA)
        A[i] = maxA;
      A[i] = (A[i] - minA) / rangeA;
    }
    for (int i = 0; i < B.length; i++) {
      if (B[i] < minB)
        B[i] = minB;
      else if (B[i] > maxB)
        B[i] = maxB;
      B[i] = (B[i] - minB) / rangeB;
    }
  }

  public double[] getAttA() {
    return this.attA;
  }

  public double[] getAttB() {
    return this.attB;
  }

  private Color[] makeColors(int num, Color leftEndColor, Color midColor,
                             Color rightEndColor) {
    ColorRampPicker picker = new ColorRampPicker();
    picker.setNSwatches(num);
    picker.setLowColor(leftEndColor);
    picker.setAnchoredColor(midColor, num / 2);
    picker.setHighColor(rightEndColor);
    return picker.getColors();
  }

  /***
   * Given a series of percentiles and the permutation values, this method will find the
   * confidence intervals for each percentile.
   * */
  private double[] makePercentileBreaks(double[] percentiles, double[] values) {
    Arrays.sort(values);
    int len = percentiles.length;
    double[] breaks = new double[len + 1];
    int pos = -1;
    for (int i = 0; i < len; i++) {
      pos = (int) (percentiles[i] * (float) (values.length));
      breaks[i] = (values[pos] + values[pos + 1]) / 2.0;
    }
    breaks[len] = Integer.MAX_VALUE;
    return breaks;
  }


  private double[] makeNormalBreaks(double[] percentiles, double[] values) {
    double avg = MathUtil.getMean(values);
    double stddev = MathUtil.getStdDev(values);
    int len = percentiles.length;
    double[] breaks = new double[len + 1];
    double val;
    for (int i = 0; i < len; i++) {
      val = MathUtil.getStdNormalCumulativeInverse(percentiles[i]);
      breaks[i] = avg + stddev * val;
    }
    breaks[len] = Integer.MAX_VALUE;
    return breaks;
  }

  private double[] makeMixedBreaks(double[] values){
    Arrays.sort(values);
    double avg = MathUtil.getMean(values);
    double stddev = MathUtil.getStdDev(values);
    System.out.println("--(mean-5stddev, mean-3stddev, p0.01, p0.05 ^ p0.05, p0.01, mean+3stddev, mean+5stddev)--");
    int p05count = (int)(0.05 * (values.length + 1)) - 1;
    int p01count = (int)(0.01 * (values.length + 1)) - 1;
    if (p05count < 0 )
      p05count = 0;
    if (p01count < 0 )
      p01count = 0;
    double[] breaks = new double[8];
    breaks[0] = avg - 5 * stddev;
    breaks[1] = avg - 3 * stddev;
    breaks[2] = values[p01count] - 0.00001;
    breaks[3] = values[p05count] - 0.00001;
    breaks[4] = values[values.length - 1 - p05count] + 0.00001;
    breaks[5] = values[values.length - 1 - p01count] + 0.00001;
    breaks[6] = avg + 3 * stddev;
    breaks[7] = avg + 5 * stddev;

    return breaks;
  }

  private double[] makeDramaticBreaks(double[] percentiles, double[] values) {
    double avg = MathUtil.getMean(values);
    double stddev = MathUtil.getStdDev(values);
    int len = percentiles.length;
    double[] breaks = new double[len + 1];
    for (int i = 0; i < len; i++) {
      if (i < len / 2 - 1)
        breaks[i] = avg + (i - len / 2) * 2 * stddev;
      else if (i == len / 2 - 1)
        breaks[i] = avg - 1.645 * stddev;
      else if (i == len / 2)
        breaks[i] = avg + 1.645 * stddev;
      else if (i > len / 2)
        breaks[i] = avg + (i - len / 2 + 1) * 2 * stddev;
    }
    breaks[len] = Integer.MAX_VALUE;
    return breaks;
  }

  private void prepareColorArrays(double[] values, double[] breaks, Color[] cs) {
    Color[] colorarray = new Color[values.length];
    for (int i = 0; i < values.length; i++) {
      int b;
      for (b = 0; b < breaks.length; b++) {
        if (values[i] < breaks[b])
          break;
      }
      colorarray[i] = cs[b];
    }
    ColorArrayEvent ce = new ColorArrayEvent(this, colorarray);
    this.fireColorArrayChanged(ce);
  }

  private void saveEntropyToFile(double[][] entropyValues, String fileName) {
    try {
      File file = new File(folder + fileName + ".csv");
      PrintWriter exportFile = new PrintWriter(new FileOutputStream(file));
      System.out.println("Entropy values are saved to \t\t" +
                         file.getAbsolutePath());
      //first two columns are X and Y
      exportFile.print("id");
      for (int s = 0; s < samplesize.length; s++) {
        exportFile.print(",Entropy" + samplesize[s]); //export
      }
      exportFile.println();
      for (int x = 0; x < entropyValues[0].length; x++) {
        exportFile.print(x);
        for (int s = 0; s < samplesize.length; s++) {
          exportFile.print("," + entropyValues[s][x]); //export
        }
        exportFile.println();
      } // end outer for()
      exportFile.close();
    } // end try
    catch (Exception ee) {
      ee.printStackTrace();
    } // end catch
  }

  /**
   * keyPressed
   *
   * @param e KeyEvent
   */
  public void keyPressed(KeyEvent e) {
  }

  /**
   * keyReleased
   *
   * @param e KeyEvent
   */
  public void keyReleased(KeyEvent e) {
  }

  /**
   * keyTyped
   *
   * @param e KeyEvent
   */
  public void keyTyped(KeyEvent e) {
  }

  /**
   * colorArrayChanged
   *
   * @param e ColorArrayEvent
   */
  public void colorArrayChanged(ColorArrayEvent e) {
  }

  /**
   * selectionChanged
   *
   * @param e SelectionEvent
   */
  public void selectionChanged(SelectionEvent e) {
    int[] ids = e.getSelection();
    int n = ids.length;
    if (n == 1) {
      if (kNN != null) {
        int s = this.samplesize.length - 1;
        int k = this.samplesize[s];
        int[] nnSelection = new int[k];
        System.arraycopy(kNN[s][ids[0]], 0, nnSelection, 0, k);
        SelectionEvent eee = new SelectionEvent(this, nnSelection);
        this.fireSelectionChanged(eee);
      }
    }
    else if (n > 1) {
      int s = this.samplesize.length - 1;
      int k = this.samplesize[s];
      int[] nnSelection = new int[k * n];
      for (int i = 0; i < n; i++) {
        System.arraycopy(kNN[s][ids[i]], 0, nnSelection, i * k, k);
      }
      SelectionEvent eee = new SelectionEvent(this, findUniqueIds(nnSelection));
      this.fireSelectionChanged(eee);
    }
  }

  private int[] findUniqueIds(int[] selection) {
    Arrays.sort(selection);
    int currentId = selection[0];
    int count = 1;
    for (int i = 1; i < selection.length; i++) {
      if (selection[i] != currentId) {
        count++;
        currentId = selection[i];
        selection[count] = selection[i];
      }
    }
    int uniqueIds[] = new int[count];
    System.arraycopy(selection, 0, uniqueIds, 0, count);
    return uniqueIds;
  }

  public void indicationChanged(IndicationEvent e) {
  }

  /**
   * mouseDragged
   *
   * @param e MouseEvent
   */
  public void mouseDragged(MouseEvent e) {
  }

  /**
   * mouseMoved
   *
   * @param e MouseEvent
   */
  public void mouseMoved(MouseEvent e) {
  }

  /**
   * mouseClicked
   *
   * @param e MouseEvent
   */
  public void mouseClicked(MouseEvent e) {
  }

  /**
   * mouseEntered
   *
   * @param e MouseEvent
   */
  public void mouseEntered(MouseEvent e) {
  }

  /**
   * mouseExited
   *
   * @param e MouseEvent
   */
  public void mouseExited(MouseEvent e) {
  }

  /**
   * mousePressed
   *
   * @param e MouseEvent
   */
  public void mousePressed(MouseEvent e) {
  }

  /**
   * mouseReleased
   *
   * @param e MouseEvent
   */
  public void mouseReleased(MouseEvent e) {
  }

  private transient Vector selectionListeners;
  public synchronized void addSelectionListener(SelectionListener l) {
    Vector v = selectionListeners == null ? new Vector(2) :
        (Vector) selectionListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      selectionListeners = v;
    }
  }

  public synchronized void removeSelectionListener(SelectionListener l) {
    if (selectionListeners != null && selectionListeners.contains(l)) {
      Vector v = (Vector) selectionListeners.clone();
      v.removeElement(l);
      selectionListeners = v;
    }
  }

  protected void fireSelectionChanged(SelectionEvent e) {
    if (selectionListeners != null) {
      Vector listeners = selectionListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ( (SelectionListener) listeners.elementAt(i)).selectionChanged(e);
      }
    }
  }

  private transient Vector colorArrayListeners;
  public synchronized void removeColorArrayListener(ColorArrayListener l) {
    if (colorArrayListeners != null && colorArrayListeners.contains(l)) {
      Vector v = (Vector) colorArrayListeners.clone();
      v.removeElement(l);
      colorArrayListeners = v;
    }
  }

  public synchronized void addColorArrayListener(ColorArrayListener l) {
    Vector v = colorArrayListeners == null ? new Vector(2) :
        (Vector) colorArrayListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      colorArrayListeners = v;
    }
  }

  protected void fireColorArrayChanged(ColorArrayEvent e) {
    if (colorArrayListeners != null) {
      Vector listeners = colorArrayListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ( (ColorArrayListener) listeners.elementAt(i)).colorArrayChanged(e);
      }
    }
  }

}
