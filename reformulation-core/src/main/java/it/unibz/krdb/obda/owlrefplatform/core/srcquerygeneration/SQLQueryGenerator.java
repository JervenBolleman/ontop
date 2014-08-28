package it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.OBDAException;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * A general interface which should be use to implement new source query
 * generation which can than be integrated in to a technique wrapper
 * 
 * @author Manfred Gerstgrasser
 * 
 */

public interface SQLQueryGenerator extends Serializable {

	/**
	 * Translates the given datalog program into a source query, which can later
	 * be evaluated by a evaluation engine.
	 * 
	 * @param query
	 *            the datalog program
	 * @return the souce query
	 * @throws Exception
	 */
	public String generateSourceQuery(DatalogProgram query, List<String> signature) throws OBDAException;
	/**
	 * Updates the current view manager with the new given parameters
	 * 
	 * @param man
	 *            the new prefix manager
	 * @param onto
	 *            the new dlliter ontology
	 * @param uris
	 *            the set of URIs of the ontologies integrated into the dlliter
	 *            ontology
	 */
	// public void update(PrefixManager man, DLLiterOntology onto, Set<URI>
	// uris);

	void setUriIds(Map<String, Integer> uriRefIds);

	// public ViewManager getViewManager();
}