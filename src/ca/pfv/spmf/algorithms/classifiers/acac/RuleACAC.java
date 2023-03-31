/* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* It was obtained from the LAC library under the GNU GPL license and adapted for SPMF.
* @Copyright original version LAC 2019   @copyright of modifications SPMF 2021
*
* SPMF is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* SPMF is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with SPMF.  If not, see <http://www.gnu.org/licenses/>.
* 
*/
package ca.pfv.spmf.algorithms.classifiers.acac;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import ca.pfv.spmf.algorithms.ArraysAlgos;
import ca.pfv.spmf.algorithms.classifiers.data.Dataset;
import ca.pfv.spmf.algorithms.classifiers.data.Instance;
import ca.pfv.spmf.algorithms.classifiers.general.Rule;

/**
 * This is a rule for the ACAC classifier. It extends the basic Rule class to
 * calculate and store the all-confidence and information gain of each rule.
 * 
 * @see Rule
 * @see AlgoACAC
 */
public class RuleACAC extends Rule implements Serializable {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 9068577561757816896L;

	/**
	 * Maximum support among items. It has to be saved to be able to calculate the
	 * all-confidence
	 */
	private long supportMax;

	/**
	 * Support per klass, used to calculate information gain while performing a
	 * prediction. Key = class Value = support
	 */
	private Map<Short, Long> supportRuleByKlass;

	/**
	 * Constructor
	 * 
	 * @param antecedent itemset forming the antecedent of the rule
	 */
	public RuleACAC(short[] antecedent) {
		add(antecedent);
		this.supportRuleByKlass = new HashMap<Short, Long>();
	}

	/**
	 * Constructor to clone a rule
	 * 
	 * @param rule a rule
	 */
	public RuleACAC(RuleACAC rule) {
		super(rule.klass);
		add(rule.antecedent);

		supportAntecedent = rule.supportAntecedent;
		supportKlass = rule.supportKlass;
		supportRule = rule.supportRule;
		supportMax = rule.supportMax;
		supportRuleByKlass = new HashMap<Short, Long>(rule.supportRuleByKlass);
	}

	/**
	 * This method read a dataset to compute the support, 
	 * supportKlass, and support for each class contained.
	 * 
	 * @param train a training dataset, used to calculate the supports
	 */
	public void evaluate(Dataset train) {
		supportAntecedent = 0;
		supportRule = 0;
		supportKlass = 0;

		// For each record
		for (Instance instance : train.getInstances()) {
			Short[] items = instance.getItems();
			short instanceKlass = instance.getKlass();

			boolean matchConsequent = instanceKlass == klass;

			if (matchConsequent) {
				supportKlass++;
			}

			boolean matchAntecedent = ArraysAlgos.isSubsetOf(antecedent, items);
			if (matchAntecedent) {
				supportAntecedent++;
			}

			if (matchAntecedent && matchConsequent) {
				supportRule++;
				long count = supportRuleByKlass.getOrDefault(instanceKlass, 0L);
				supportRuleByKlass.put(instanceKlass, count + 1);
			}
		}
	}

	/**
	 * Get all-confidence metric of the current rule
	 * 
	 * @return the value for this metric
	 */
	public double getAllConfidence() {
		if (this.getAntecedent().size() == 1)
			return 1.0;
		if (this.supportMax <= 0)
			return Double.NaN;

		return this.supportRule / (double) this.supportMax;
	}

	/**
	 * When this rule is generated, it is generated by the combinations of two rules
	 * of size k-1. Its all-confidence will use the maximum value of support from
	 * those two parents
	 * 
	 * @param supportRule1 support of first parent of size k-1
	 * @param supportRule2 support of second parent of size k-1
	 */
	void setMaximums(long supportRule1, long supportRule2) {
		this.supportMax = Long.max(supportRule1, supportRule2);
	}

	/**
	 * Get support for the class specified as parameter. It will be used to
	 * calculate informationGain on the classifier.
	 * 
	 * @param klass to obtain its support
	 * @return the support for the class
	 */
	public long getSupportByKlass(short klass) {
		return this.supportRuleByKlass.getOrDefault(klass, 0L);
	}

	@Override
	public String getMeasuresToString() {
		return " #SUP: " + getSupportRule() + " #CONF: " + getConfidence() + " #ALLCONF: " + this.getAllConfidence();
	}
}
