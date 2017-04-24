/*******************************************************************************
 * Copyright (c) 2006 Bioclipse Project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rob Schellhorn
 ******************************************************************************/

package net.bioclipse.ghemical.model;

import static org.openscience.cdk.tools.manipulator.ChemFileManipulator.getAllAtomContainers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.bioclipse.model.CDKResource;
import net.bioclipse.model.IBioResource;

import org.openscience.cdk.config.Symbols;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemFile;

/**
 * CDKResourceAdapter wraps CDKResource objects and provides facilities to
 * easily access its properties. The CDKResource is only parsed when needed and
 * parsing occurs only once.
 * <p>
 * CDKResourceAdapter objects maintain a state:
 * <ol>
 * <li>Not parsed: the adapter has not tried to retrieve information from the
 * resource. Each object starts in this state.</li>
 * <li>Parsing failed: the needed information could not be retrieved from the
 * resource. Objects in this state will never leave it.</li>
 * <li>Parsing succeeded: the adapter has successfully retrieved the needed
 * information from the resource. Objects will never leave this state.</li>
 * </ol>
 * </p>
 * <p>
 * When the encapsulated CDKResource is changed after being parsed, this change
 * will not be reflected in this adapter class. Instances should therefore only
 * live during one operation and not be reused later. Also note that this class
 * is not thread safe, correct functioning is not guaranteed when accessed from
 * multiple threads concurrently.
 * </p>
 * 
 * @author Rob Schellhorn
 * @see CDKResource
 * @see CDKResourceAdapter.State
 * @see IAtomContainer
 */
public class CDKResourceAdapter {

	/**
	 * The possible states for CDKResourceAdapter instances.
	 */
	protected enum State {
		NOT_PARSED, PARSED_FAILED, PARSED_OK;
	}

	private static final Map<String, Integer> symbols = new HashMap<String, Integer>();

	static {
		for (int i = 0; i < Symbols.byAtomicNumber.length; i++) {
			symbols.put(Symbols.byAtomicNumber[i], i);
		}
	}

	/**
	 * @param adapter
	 * @return
	 */
	public static double getAverageScaleFactor(CDKResourceAdapter adapter) {
		if (adapter == null) {
			return 0;
		}

		try {
			return BondUtil.getAverageScalingFactor(adapter.getBonds());
		} catch (AdapterException e) {
			return 0;
		}
	}

	/**
	 * When parsed, this array contains the atoms of the molecule represented by
	 * the resource.
	 */
	private IAtom[] atoms;

	/**
	 * When parsed, this array contains the bonds of the molecule represented by
	 * the resource.
	 */
	private IBond[] bonds;

	/**
	 * 
	 */
	private IChemFile file;

	/**
	 * The encapsulated resource from which the atoms and bonds are extracted.
	 */
	private final CDKResource resource;

	/**
	 * 
	 */
	private double scaleFactor = 1;

	/**
	 * The current state of this instance.
	 */
	private State state = State.NOT_PARSED;

	/**
	 * Creates a new CDKResourceAdapter for the given resource. This resource is
	 * an instance, so not<code>null</code>. After construction the state of
	 * this adapter is not parsed.
	 * 
	 * @param resource
	 *            The resource to adapt.
	 * @throws IllegalArgumentException
	 */
	public CDKResourceAdapter(CDKResource resource) {
		if (resource == null) {
			throw new IllegalArgumentException("The resource is null"); //$NON-NLS-1$
		}

		this.resource = resource;

		assert state == State.NOT_PARSED : "POST: The resource has not been parsed";
		assert invariant() : "POST: The invariant holds";
	}

	/**
	 * @return
	 * @throws AdapterException
	 */
	public IChemFile cloneResource() throws AdapterException {
		assert invariant() : "PRE: The invariant holds"; //$NON-NLS-1$

		if (state == State.NOT_PARSED) {
			parse();
		}

		if (state == State.PARSED_FAILED) {
			throw new AdapterException();
		}

		try {
			return (IChemFile) file.clone();
		} catch (Exception e) {
			throw new AdapterException(e);
		}
	}

	/**
	 * Retrieves the atoms from the encapsulated resource. The array will not
	 * contain any Hydrogen atoms.
	 * 
	 * @return An array containing the atoms.
	 * @throws AdapterException
	 *             Raised if the atoms could not be retrieved from the resource.
	 */
	public IAtom[] getAtoms() throws AdapterException {
		assert invariant() : "PRE: The invariant holds"; //$NON-NLS-1$

		if (state == State.NOT_PARSED) {
			parse();
		}

		if (state == State.PARSED_FAILED) {
			throw new AdapterException();
		}

		return atoms;
	}

	/**
	 * Retrieves the bonds from the encapsulated resource. The array will not
	 * contain any bonds involving a Hydrogen atom.
	 * 
	 * @return An array containing the bonds.
	 * @throws AdapterException
	 *             Raised if the bonds could not be retrieved from the resource.
	 */
	public IBond[] getBonds() throws AdapterException {
		assert invariant() : "PRE: The invariant holds"; //$NON-NLS-1$

		if (state == State.NOT_PARSED) {
			parse();
		}

		if (state == State.PARSED_FAILED) {
			throw new AdapterException();
		}

		return bonds;
	}

	/**
	 * Retrieves the name of this adapter's resource.
	 * 
	 * @return The name of the resource.
	 */
	public String getName() {
		assert invariant() : "PRE: The invariant holds"; //$NON-NLS-1$

		return resource.getName();
	}

	/**
	 * Retrieves the factor this adapter's atom coordinates needs to be scaled
	 * with to be translated into nanometers.
	 * 
	 * @return
	 */
	public double getScaleFactor() throws AdapterException {
		assert invariant() : "PRE: The invariant holds"; //$NON-NLS-1$

		return scaleFactor;
	}

	/**
	 * Returns the current state of this adapter.
	 * 
	 * @return The current state of this adapter, never <code>null</code>.
	 */
	protected State getState() {
		assert invariant() : "PRE: The invariant holds"; //$NON-NLS-1$

		return state;
	}

	/**
	 * Checks whether this instance satisfies the class invariant. To satisfy
	 * the invariant the instance must apply to ALL of the following conditions:
	 * <ul>
	 * <li>The scale factor is a positive double.</li>
	 * <li>The resource is an instance.</li>
	 * <li>The internal state of this adapter is known.</li>
	 * </ul>
	 * 
	 * @return <code>true</code> if this instance satisfies the class
	 *         invariant, <code>false</code> otherwise.
	 */
	private boolean invariant() {
		return scaleFactor > 0 && resource != null && state != null;
	}

	/**
	 * @return
	 */
	protected boolean isParsedOK() {
		return state == State.PARSED_OK;
	}

	/**
	 * Parses the resource and caches the properties of this adapter.
	 * <p>
	 * Note that this method should only be called once.
	 * </p>
	 * 
	 * @throws AdapterException
	 *             If this adapter could not parse the embedded resource.
	 * @throws IllegalStateException
	 *             If this method already has been invoked before.
	 */
	@SuppressWarnings("unchecked")
	protected void parse() throws AdapterException {
		assert invariant() : "PRE: The invariant holds";

		if (state != State.NOT_PARSED) {
			throw new IllegalStateException("Already tried parsing"); //$NON-NLS-1$
		}

		// Assume parsing fails
		state = State.PARSED_FAILED;

		// First make sure the resource is loaded and parsed
		IBioResource parsedResource = resource.loadParseParseTypes();
		if (parsedResource == null) {
			throw new AdapterException("The resource could not be loaded"); //$NON-NLS-1$
		}

		// Then extract the needed properties from the parsed resource
		Object file = parsedResource.getParsedResource();
		if (!(file instanceof IChemFile)) {
			throw new AdapterException("The parsed resource is not a ChemFile"); //$NON-NLS-1$
		}

		this.file = (IChemFile) file;

		Set<IAtom> atomSet = new HashSet<IAtom>();
		Set<IBond> bondSet = new HashSet<IBond>();

		// Add for every container its atoms and bonds to this adapter
		for (Object o : getAllAtomContainers(this.file)) {
			IAtomContainer container = (IAtomContainer) o;

			for(Iterator<IAtom> iterator = container.atoms(); iterator.hasNext();) {
				IAtom atom = iterator.next();
				
				if (atom.getPoint3d() == null) {
					throw new AdapterException("Atom has no 3d coordinates");
				}

				if (atom.getAtomicNumber() < 1) {
					Integer atomicNumber = symbols.get(atom.getSymbol());
					if (atomicNumber == null) {
						throw new AdapterException(
								"No atom known by that symbol");
					}

					atom.setAtomicNumber(atomicNumber);
				}

				if (!atomSet.add(atom)) {
					throw new AdapterException(
							"Atom could not be added to the set");
				}
			}

			for (IBond bond : container.getBonds()) {
				if (bond.getAtomCount() != 2) {
					throw new AdapterException(
							"Only support bonds with two atoms");
				}

				IAtom begin = bond.getAtom(0);
				IAtom end = bond.getAtom(1);

				if (!atomSet.contains(begin) || !atomSet.contains(end)) {
					throw new AdapterException(
							"At least one of the atoms is not in the atom set");
				}

				if (!bondSet.add(bond)) {
					throw new AdapterException(
							"Bond could not be added to the set");
				}
			}
		}

		atoms = atomSet.toArray(new IAtom[atomSet.size()]);
		bonds = bondSet.toArray(new IBond[bondSet.size()]);

		double scaleFactor = BondUtil.getAverageScalingFactor(bonds);
		if (scaleFactor != 0) {
			this.scaleFactor = scaleFactor;
		} else {
			// TODO Warn: scale factor could not be resolved
		}

		// Parsing is successful
		state = State.PARSED_OK;

		assert atoms != null : "POST: The atoms array is set"; //$NON-NLS-1$
		assert bonds != null : "POST: The bonds array is set"; //$NON-NLS-1$
		assert isParsedOK() : "POST: The resource has been parsed successful"; //$NON-NLS-1$
		assert invariant() : "POST: The invariant holds"; //$NON-NLS-1$
	}

	/**
	 * @param id
	 * @param value
	 * @throws AdapterException
	 */
	public void setProperty(String id, Object value) throws AdapterException {
		assert invariant() : "PRE: The invariant holds"; //$NON-NLS-1$

		if (state == State.NOT_PARSED) {
			parse();
		}

		if (state == State.PARSED_FAILED) {
			throw new IllegalStateException();
		}

		file.setProperty(id, value);

		assert invariant() : "POST: The invariant holds"; //$NON-NLS-1$
	}

	/**
	 * Sets the scale factor for the embedded CDKResource. This scale factor
	 * will be used to translate the coordinates of the atoms to nanometers.
	 * 
	 * @param scaleFactor
	 *            The new scale factor for the adapter.
	 * @throws IllegalArgumentException
	 *             If the given scale factor is invalid.
	 */
	public void setScaleFactor(double scaleFactor) {
		assert invariant() : "PRE: The invariant holds"; //$NON-NLS-1$

		if (scaleFactor <= 0) {
			throw new IllegalArgumentException();
		}

		this.scaleFactor = scaleFactor;

		assert invariant() : "POST: The invariant holds"; //$NON-NLS-1$
	}
}