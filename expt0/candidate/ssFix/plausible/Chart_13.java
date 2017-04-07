/*
Wotonomy: OpenStep design patterns for pure Java applications.
Copyright (C) 2000 Blacksmith, Inc.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, see http://www.gnu.org
*/

package net.wotonomy.ui.swing.util;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

/**
* This class will actively check the inputs of 2 numbers in seperate text
* components.  The number in the text components represent an upper and lower
* bound to some range.  This class checks to make sure the user inputs values
* in the lower bound text field that are less than the value of the upper
* bound and vice versa for the upper bound text field.  This class will also
* check to make sure the bounds fall within a given range if specified.
*
* The checks are automatically performed when the focus is lost on either
* component.  If the inputs are correct then no event occurs.  If the inputs
* are not correct, then a dialog message is displayed stating the reason why
* the bounds are invalid, and the original correct value is restored into the
* text components.
*
* @author rglista
* @author $Author$
* @version $Revision$
*/
public class TextInputRangeChecker implements FocusListener
{
    protected static final int NONE = 0;
    protected static final int LOWER = 1;
    protected static final int UPPER = 2;
    
    private JTextComponent lowerComponent;
    private JTextComponent upperComponent;
    private double maxRange;
    private double lowerNumber;
    private double upperNumber;
    private Collection focusListeners;

    private String invalidLowerMessage;
    private String invalidUpperMessage;
    private String invalidEitherMessage;
    private String invalidRangeMessage;

    
/**
* Constructor with some of the settable parameters.  No range checking is used.
* @param aLowerTextComponent A text component for the lower bound.
* @param anUpperTextComponent A text component for the upper bound.
*/
    public TextInputRangeChecker( JTextComponent aLowerTextComponent,
                                  JTextComponent anUpperTextComponent )
    {
        this( aLowerTextComponent, anUpperTextComponent, null, null, 0.0 );
    }

/**
* Constructor with some of the settable parameters.  No range checking is
* used.
* @param aLowerTextComponent A text component for the lower bound.
* @param anUpperTextComponent A text component for the upper bound.
* @param lowerTextName The name of the lower bound, eg - start year.
* @param upperTextName The name of the upper bound, eg - end year.
*        is used.
*/
    public TextInputRangeChecker( JTextComponent aLowerTextComponent,
                                  JTextComponent anUpperTextComponent,
                                  String lowerTextName, String upperTextName )
    {
        this( aLowerTextComponent, anUpperTextComponent, lowerTextName, upperTextName, 0.0 );
    }

/**
* Constructor with some of the settable parameters.
* @param aLowerTextComponent A text component for the lower bound.
* @param anUpperTextComponent A text component for the upper bound.
* @param aMaxRange The range the bounds muist fall between, if 0 then no range
*        is used.
*/
    public TextInputRangeChecker( JTextComponent aLowerTextComponent,
                                  JTextComponent anUpperTextComponent,
                                  double aMaxRange )
    {
        this( aLowerTextComponent, anUpperTextComponent, null, null, aMaxRange );
    }

/**
* Constructor with all the settable parameters.
* @param aLowerTextComponent A text component for the lower bound.
* @param anUpperTextComponent A text component for the upper bound.
* @param lowerTextName The name of the lower bound, eg - start year.
* @param upperTextName The name of the upper bound, eg - end year.
* @param aMaxRange The range the bounds muist fall between, if 0 then no range
*        is used. 
*/
    public TextInputRangeChecker( JTextComponent aLowerTextComponent,
                                  JTextComponent anUpperTextComponent,
                                  String lowerTextName, String upperTextName,
                                  double aMaxRange )
    {
        lowerComponent = aLowerTextComponent;
        upperComponent = anUpperTextComponent;
        maxRange = aMaxRange;

        focusListeners = new ArrayList( 1 );  // For most cases, there will be only 1 listener.

        lowerComponent.addFocusListener( this );
        upperComponent.addFocusListener( this );

        lowerNumber = getNumber( lowerComponent );
        upperNumber = getNumber( upperComponent );

        if ( ( lowerTextName != null ) && ( upperTextName != null ) )
        {
            invalidLowerMessage = "The " + lowerTextName + " must be less than or equal to the " + upperTextName + ".";
            invalidUpperMessage = "The " + upperTextName + " must be greater than or equal to the " + lowerTextName + ".";
            invalidEitherMessage = "The " + lowerTextName + " and/or the " + upperTextName + " are not correct.";
            invalidRangeMessage = "The maximum range for the " + lowerTextName + " and " + upperTextName + " is " + maxRange + ".";
        }
        else
        {
            invalidLowerMessage = "The lower bound must be less than or equal to the upper bound.";
            invalidUpperMessage = "The upper bound must be greater than or equal to the lower bound.";
            invalidEitherMessage = "The upper and/or lower bounds are not correct.";
            invalidRangeMessage = "The maximum range is " + maxRange + ".";
        }
    }

/**
* Allows the caller to perform the validation of the bounds programatically.
* The lower bound is compared to the upper bound and range checking is performed.
* If the lower bound is greater than the upper bound, or the range between the
* bounds is greater than the max range, then validation fails.
* @return TRUE is validation is successfull, FALSE if it fails.
*/
    public boolean performCheck()
    {
        return validate( null );
    }

/**
* Adds the listener to the lists of focus listener maintened by this object.
* When one of the 2 text components receives a focus event, this object will
* fire that focus event to any of its listeners.  This is useful when the
* calling object wants to be notified of the components focus events, but wants
* to ensure that the validation has occured first.
* <br><br>
* NOTE: The focus is only fired if the validation was successful.  This might
*       have to be changed.
* @param aListener A Focus Listener to receive Focus Events.
*/
    public void addFocusListener( FocusListener aListener )
    {
        focusListeners.add( aListener );
    }

/**
* Returns the last valid value of the lower bound.  If this is called while
* the user is updating the text component but before the focus is lost, the
* value returned will be the original value before the user started updating
* the bound.
* @return The last valid value of the lower bound. 
*/
    public double getLastValidatedLowerNumber()
    {
        return lowerNumber;
    }

/**
* Returns the last valid value of the upper bound.  If this is called while
* the user is updating the text component but before the focus is lost, the
* value returned will be the original value before the user started updating
* the bound.
* @return The last valid value of the upper bound.
*/
    public double getLastValidatedUpperNumber()
    {
        return upperNumber;
    }

/**
* Method used to be notified when one of the text components has gained its
* focus.
*/
    public void focusGained( FocusEvent e )
    {
        lowerNumber = getNumber( lowerComponent );
        upperNumber = getNumber( upperComponent );
    }

/**
* Method used to be notified when one of the text components has lost its
* focus.  Automatic validation occurs here.
*/
    public void focusLost( FocusEvent e )
    {
        if ( e.isTemporary() )
        {
            return;
        }

        if ( validate( e.getSource() ) )
        {
            fireFocusEvent( e );
        }
    }

/**
* Fires a focus lost event if the validation was successfull.
*/
    protected void fireFocusEvent( FocusEvent e )
    {
        for ( Iterator it = focusListeners.iterator(); it.hasNext(); )
        {
            ( ( FocusListener )it.next() ).focusLost( e );
        }
    }

/**
* Validates the bounds inputed by the user.
* @param aComponent The component to use to display a dialog window, if neccessray.
*        If null, then the parent window of the text componets will be used. 
* @return TRUE if validation was successful, FALSE otherwise.
*/
    protected boolean validate( Object aComponent )
    {
        int componentUsed = NONE;
        if ( aComponent == lowerComponent )
        {
            componentUsed = LOWER;
        }
        else if ( aComponent == upperComponent )
        {
            componentUsed = UPPER;
        }

        double lower = getNumber( lowerComponent );
        double upper = getNumber( upperComponent );

        if ( lower > upper )
        {
            if ( componentUsed == LOWER )
            {
                lowerComponent.setText( Double.toString( lowerNumber ) );
                displayMessage( invalidLowerMessage, lowerComponent );
            }
            else if ( componentUsed == UPPER )
            {
                upperComponent.setText( Double.toString( upperNumber ) );
                displayMessage( invalidUpperMessage, upperComponent );
            }
            else
            {
                upperComponent.setText( Double.toString( upperNumber ) );
                lowerComponent.setText( Double.toString( lowerNumber ) );
                displayMessage( invalidEitherMessage, lowerComponent.getTopLevelAncestor() );
            }

            return false;
        }

        if ( maxRange != 0.0 )
        {
            if ( ( upper - lower ) > maxRange )
            {
                if ( componentUsed == LOWER )
                {
                    lowerComponent.setText( Double.toString( lowerNumber ) );
                    displayMessage( invalidRangeMessage, lowerComponent );
                }
                else if ( componentUsed == UPPER )
                {
                    upperComponent.setText( Double.toString( upperNumber ) );
                    displayMessage( invalidRangeMessage, upperComponent );
                }
                else
                {
                    upperComponent.setText( Double.toString( upperNumber ) );
                    lowerComponent.setText( Double.toString( lowerNumber ) );
                    displayMessage( invalidRangeMessage, lowerComponent.getTopLevelAncestor() );
                }

                return false;
            }
        }

        lowerNumber = lower;
        upperNumber = upper;
        return true;
    }

/**
* Creates a JOptionPane to display the reason why the bounds failed validation.
*/
    protected void displayMessage( final String message, final Component parent )
    {
        SwingUtilities.invokeLater( new Runnable()
        {
            public void run()
            {
                JOptionPane.showMessageDialog( parent, message, "Data Entry Error",
                                               JOptionPane.ERROR_MESSAGE );
            }
        } );
    }

/**
* Gets the number represented in the text component.  If the text does not
* represent a number, then zero is returned.
*/
    protected double getNumber( JTextComponent aComponent )
    {
        try
        {
            return Double.valueOf( aComponent.getText() ).doubleValue();
//1.2            return Double.parseDouble( aComponent.getText() );
        }
        catch ( NumberFormatException e )
        {
            System.out.println("[GUI] TextInputRangeChecker.getNumber(): The text is NOT a number: " + aComponent.getText() );
            return 0.0;
        }
    }
}

/*
 * $Log$
 * Revision 1.2  2006/02/18 23:19:05  cgruber
 * Update imports and maven dependencies.
 *
 * Revision 1.1  2006/02/16 13:22:22  cgruber
 * Check in all sources in eclipse-friendly maven-enabled packages.
 *
 * Revision 1.2  2003/08/06 23:07:53  chochos
 * general code cleanup (mostly, removing unused imports)
 *
 * Revision 1.1.1.1  2000/12/21 15:51:49  mpowers
 * Contributing wotonomy.
 *
 * Revision 1.2  2000/12/20 16:25:46  michael
 * Added log to all files.
 *
 *
 */

