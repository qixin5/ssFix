package org.openweather.station;



/**
 * <p>Title: WindDirectionSensor</p>
 * <p>Description: Create a new WindDirectionSensor</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p/>
 * <p>Base class for wind direction sensors
 * @author Rich Mulvey
 * @version 1.0
 */

public class WindDirectionSensor extends OneWireDevice {
    
    /** The offset to true north, in the direction array
     */
    protected int northOffset = 1;
    /** The current wind direction
     */
    protected int windDirection = 0;
    /** List of all the device ID's in the direction array
     */
    
    /** Bins to store the wind directions in */
    public int directionBins[] = new int[16];
    
    
    /**
     * Create a new wind direction sensor
     * @param hubChannel The hubChannel associated with the sensor
     * @param adapter The device adapter to use
     * @param deviceAddress The device address to use
     * @param localName An alternate name for the sensor
     * @param debugServer The SocketServer handling the debug messages
     * @throws OneWireException Any 1-wire exception
     */
    public WindDirectionSensor(com.dalsemi.onewire.adapter.DSPortAdapter adapter, String deviceAddress, String localName, SocketServer debugServer, HubChannelMap hubChannel) throws com.dalsemi.onewire.OneWireException {
        
        super( adapter, deviceAddress, localName, debugServer, hubChannel );
        
        if (device == null) {
            debug("No wind direction sensor found with deviceAddress " + deviceAddress );
        }
        
        this.northOffset = northOffset;
        debug( "North offset is " + northOffset );
        clearDirectionBins();
    }
    
    
    /** Read the wind direction
     * @throws OneWireException Any 1-wire exception
     * @return The wind direction
     */
    public String hardwareRead() throws com.dalsemi.onewire.OneWireException {
        return "Base Class Implementation";
    }
    
    
    /** Get the wind direction
     * @return The wind direction
     */
    public String getStringValue() {
        return getDirectionAsString( windDirection );
    }
    
    
    /* convert direction integer into compass direction string */
    /** Get the wind direction, as a string
     * @param input The direction, as an integer offset
     * @return The wind direction
     *
     */
    public String getDirectionAsString(int input) {
        String[]  direction = {" N ", "NNE", "NE ", "ENE",
                " E ", "ESE", "SE ", "SSE",
                " S ", "SSW", "SW ", "WSW",
                " W ", "WNW", "NW ", "NNW" };
                
                input = (input + northOffset ) % 16;
                
                return direction[input];
    }
    
    /**
     * Get the wind direction as a number, from 1 to 16
     * @return The wind direction as a number, from 1 to 16
     */
    public int getDirectionAsNumber() {
        return -1;
    }
    
    
    /** Get the north offset
     * @return The north offset
     */
    public int getNorthOffset() {
        return northOffset;
    }
    /** Set the north offset
     * @param northOffset The north offset
     */
    public void setNorthOffset(int northOffset) {
        this.northOffset = northOffset;
    }
    /** Get the wind direction
     * @return The wind direction
     */
    public int getWindDirection() {
        return windDirection;
    }
    
    /** Reset the counts in all of the direction bins */
    public void clearDirectionBins() {
        for( int i = 0; i < directionBins.length; i++ ) {
            directionBins[i] = 0;
        }
    }
    
    
    /**
     * Get the direction bin with the most hits
     * @return The direction bin with the most hits
     */
    public int getProcessedDirection() {
        int maxIndex = 0;
        int maxValue = -1;
        
        for( int i = 0; i < directionBins.length; i++ ) {
            
            // Check to see if we have a new maximum
            if( directionBins[i] > maxValue ) {
                maxIndex = i;
                maxValue = directionBins[i];
            }
        }
        
        return maxIndex;
        
    }
    
    public int findMaxConsensusReading() {
        int maxBaseFound = 0;
        float maxSumFound = 0;
        float sum = 0;
        float W = 0;
        int D = 0;
        
        
        // Find the span of 5 adjacent cells with the maximum sum
        for( int i = 0; i < 16; i++ ) {
//            debug( "directionBins[" + i + "] = " + directionBins[i]);
            sum = directionBins[i] +
                    directionBins[ ( i + 1 ) % 16 ] +
                    directionBins[ ( i + 2 ) % 16 ] +
                    directionBins[ ( i + 3 ) % 16 ] +
                    directionBins[ ( i + 4 ) % 16 ] ;
            
            if( sum > maxSumFound ) {
                maxBaseFound = i;
                maxSumFound = sum;
            }
            
        }

//        debug( "MaxBaseFound = " + maxBaseFound );
//        debug( "MaxSumFound = " + maxSumFound );
        
        W = ( directionBins[ (maxBaseFound + 1) % 16  ] + 2 * directionBins[ (maxBaseFound + 2) % 16 ] + 3 * 
                directionBins[ (maxBaseFound + 3) % 16 ] + 4 * directionBins[ ( maxBaseFound + 4 ) % 16 ] ) * 45 / maxSumFound;
        
//        debug( "W = " + W );
        
        D = (int) (maxBaseFound * 45 + W);
        
//        debug( "D = " + D );
        
        if( D > 720 ) {
            D = D - 720;
        }
//        debug( "D = " + D );
        D = D / 2;
//        debug( "D = " + D );
        
        return D;
        
    }
}
