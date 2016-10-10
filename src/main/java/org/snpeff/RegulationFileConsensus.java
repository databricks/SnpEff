package org.snpeff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.snpeff.fileIterator.RegulationFileIterator;
import org.snpeff.interval.Markers;
import org.snpeff.interval.Regulation;
import org.snpeff.util.Gpr;
import org.snpeff.util.Timer;

/**
 * Create a regulation consensus from a regulation file.
 *
 * @author pcingola
 */
public class RegulationFileConsensus {

	/**
	 * This class collapses adjacent intervals that appear
	 * consecutively within a regulatopn file
	 * @author pcingola
	 */
	class RegulationConsensus {
		int count = 1;
		Regulation consensus = null;

		void add(Regulation r) {
			if (consensus == null) {
				consensus = r;
				count = 1;
			} else {
				if (consensus.intersects(r)) {
					consensus.setStart(Math.max(consensus.getStart(), r.getStart()));
					consensus.setEnd(Math.max(consensus.getEnd(), r.getEnd()));
					count++;
				} else {
					flush();
					consensus = r;
					count = 1;
				}
			}
		}

		void flush() {
			if (consensus != null) {
				totalCount++;
				totalLength += consensus.size();

				List<Regulation> regs = getRegulationList(consensus.getRegulationType());
				regs.add(consensus);
			}
		}
	}

	boolean verbose = false;
	boolean regSortedByType = false;
	int totalCount = 0;
	int totalLineNum = 0;
	long totalLength = 0;
	String outputDir;
	HashMap<String, RegulationConsensus> regConsByName;
	HashMap<String, ArrayList<Regulation>> regListByRegType;

<<<<<<< HEAD
	public RegulationFileConsensus() {
=======
	public RegulationFileConsensus(boolean verbose) {
		this.verbose = verbose;
		reset();
>>>>>>> 0e2bb5ee2b3f56f7ade37e7c6c88f814308cfbd9
	}

	/**
	 * Add to consensus
	 */
	public void consensus(Regulation reg) {
		String name = reg.getName();
		String regType = reg.getRegulationType();
		String key = regType + "_" + name;

		// Get or create
		RegulationConsensus regCons = regConsByName.get(key);
		if (regCons == null) {
			regCons = new RegulationConsensus();
			regConsByName.put(key, regCons);
		}

		regCons.add(reg);
	}

	public void createDatabases(RegulationFileIterator regulationFileIterator) {
		readFile(regulationFileIterator); // Read info from file
		save(); // Save database
	}

	// Flush all add all consensus intervals to the lists
	void flush() {
		for (RegulationConsensus regCons : regConsByName.values())
			regCons.flush();
	}

	public Collection<String> getRegTypes() {
		return regListByRegType.keySet();
	}

	/**
	 * Get regulation list by type (or create a new list)
	 */
	public ArrayList<Regulation> getRegulationList(String regType) {
		ArrayList<Regulation> regs = regListByRegType.get(regType);
		if (regs == null) {
			if (verbose) Timer.showStdErr("\tAdding regulatory type: '" + regType + "'");
			regs = new ArrayList<>();
			regListByRegType.put(regType, regs);
		}
		return regs;
	}

	/**
	 * Read a file and add all regulation intervals
	 */
	public void readFile(RegulationFileIterator regulationFileIterator) {
		String chromo = "";
		int lineNum = 1;
		String regType = "";
		for (Regulation reg : regulationFileIterator) {

			// Different chromosome? flush all
			if (!chromo.equals(reg.getChromosomeName())) flush();

			// Regulation type different than previous one?
			if (regSortedByType //
					&& !reg.getRegulationType().equals(regType) //
					&& !regType.isEmpty() //
			) {
				// Save regulation markers
				Gpr.debug("TYPE: '" + reg.getRegulationType() + "' vs '" + regType + "'");
				save();
				reset();
			}

			// Create consensus
			consensus(reg);

			// Prepatre for next iteration
			lineNum++;
			totalLineNum++;
			regType = reg.getRegulationType();
			chromo = reg.getChromosomeName();
		}

		// Finished, flush all (add all consensus intervals to the lists)
		flush();

		// Show stats
		if (verbose) {
			Timer.showStdErr("Done");
			double perc = (100.0 * totalCount / totalLineNum);
			System.err.println("\tTotal lines                 : " + lineNum);
			System.err.println("\tTotal annotation count      : " + totalCount);
			System.err.println("\tPercent                     : " + String.format("%.1f%%", perc));
			System.err.println("\tTotal annotated length      : " + totalLength);
			System.err.println("\tNumber of cell/annotations  : " + regConsByName.size());
		}
	}

	void reset() {
		regConsByName = new HashMap<>();
		regListByRegType = new HashMap<>();
	}

	/**
	 * Save databases (one file per regType)
	 */
	public void save() {
		flush();

		for (String regType : regListByRegType.keySet()) {
			Markers markersToSave = new Markers();
			markersToSave.addAll(regListByRegType.get(regType));

			if (!markersToSave.isEmpty()) {
				String rType = Gpr.sanityzeFileName(regType);
				String fileName = outputDir + "/regulation_" + rType + ".bin";
				if (verbose) Timer.showStdErr("\tSaving database '" + regType + "' (" + markersToSave.size() + " markers) in file '" + fileName + "'");

				// Save markers to file
				markersToSave.save(fileName);
			}
		}
	}

<<<<<<< HEAD
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
=======
	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	public void setRegSortedByType(boolean regSortedByType) {
		this.regSortedByType = regSortedByType;
>>>>>>> 0e2bb5ee2b3f56f7ade37e7c6c88f814308cfbd9
	}

	void show(Regulation reg) {
		System.out.println(reg);
	}

}
