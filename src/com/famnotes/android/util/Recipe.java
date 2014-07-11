package com.famnotes.android.util;

import java.io.Serializable;

public class Recipe implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private int recipeID;
	private String recipeName;
	private boolean isChecked = false;	//该菜单是否在ListView中被选中
	
//	public Recipe(int recipeID, String recipeName) {
//		this.recipeID = recipeID;
//		this.recipeName = recipeName;
//	}
	
	public int getRecipeID() {
		return recipeID;
	}
	public void setRecipeID(int recipeID) {
		this.recipeID = recipeID;
	}
	public String getRecipeName() {
		return recipeName;
	}
	public void setRecipeName(String recipeName) {
		this.recipeName = recipeName;
	}
	public int getID() {
		return id;
	}
	public void setID(int id) {
		this.id = id;
	}
	public boolean isChecked() {
		return isChecked;
	}
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

}
