/*
 * Online Structure Learner by Revision (OSLR) is an online relational
 * learning algorithm that can handle continuous, open-ended
 * streams of relational examples as they arrive. We employ
 * techniques from theory revision to take advantage of the already
 * acquired knowledge as a starting point, find where it should be
 * modified to cope with the new examples, and automatically update it.
 * We rely on the Hoeffding's bound statistical theory to decide if the
 * model must in fact be updated accordingly to the new examples.
 * The system is built upon ProPPR statistical relational language to
 * describe the induced models, aiming at contemplating the uncertainty
 * inherent to real data.
 *
 * Copyright (C) 2017-2018 Victor Guimarães
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/* Generated By:JavaCC: Do not edit this line. KnowledgeParserConstants.java */


package br.ufrj.cos.logic.parser.knowledge;

/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface KnowledgeParserConstants {

  /**
   * End of File.
   */
  int EOF = 0;
  /**
   * RegularExpression Id.
   */
  int DECIMAL = 6;
  /**
   * RegularExpression Id.
   */
  int INTEGER = 7;
  /**
   * RegularExpression Id.
   */
  int NEGATION = 8;
  /**
   * RegularExpression Id.
   */
  int VARIABLE = 9;
  /**
   * RegularExpression Id.
   */
  int CONSTANT = 10;
  /**
   * RegularExpression Id.
   */
  int OPEN_PREDICATE_ARGUMENT = 11;
  /**
   * RegularExpression Id.
   */
  int CLOSE_PREDICATE_ARGUMENT = 12;
  /**
   * RegularExpression Id.
   */
  int LIST_SEPARATOR = 13;
  /**
   * RegularExpression Id.
   */
  int WEIGHT_SEPARATOR = 14;
  /**
   * RegularExpression Id.
   */
  int OPEN_FEATURES = 15;
  /**
   * RegularExpression Id.
   */
  int CLOSE_FEATURES = 16;
  /**
   * RegularExpression Id.
   */
  int IMPLICATION_SIGN = 17;
  /**
   * RegularExpression Id.
   */
  int END_OF_LINE_CHARACTER = 18;
  /**
   * RegularExpression Id.
   */
  int ARITY_SEPARATOR_CHARACTER = 19;
  /**
   * RegularExpression Id.
   */
  int FUNCTION_DEFINITION_SIGN = 20;
  /**
   * RegularExpression Id.
   */
  int QUOTED = 21;

  /**
   * Lexical state.
   */
  int DEFAULT = 0;

  /**
   * Literal token values.
   */
  String[] tokenImage = {
          "<EOF>",
          "\" \"",
          "\"\\t\"",
          "\"\\r\"",
          "\"\\n\"",
          "<token of kind 5>",
          "<DECIMAL>",
          "<INTEGER>",
          "\"not\"",
          "<VARIABLE>",
          "<CONSTANT>",
          "\"(\"",
          "\")\"",
          "\",\"",
          "\"::\"",
          "\"{\"",
          "\"}\"",
          "\":-\"",
          "\".\"",
          "\"/\"",
          "\"#\"",
          "<QUOTED>",
  };

}
