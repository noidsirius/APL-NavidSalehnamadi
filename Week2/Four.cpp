#include<iostream>
#include<fstream>
#include<vector>
#include<map>
#include<set>
#include<sstream>
#include<algorithm>
using namespace std;


// General Helper Functions
bool isBlank(string word){
	for(int i=0; i<word.length(); i++)
		if(!isspace(word[i]))
			return false;
	return true;
}

bool sortbysec(const pair<string,int> &a, const pair<string,int> &b) 
{ 
    return (a.second > b.second); 
} 

// Primary procedures

void get_whole_text(string file_name, string &text){
	ifstream text_file;
	text_file.open(file_name.c_str());
	if(text_file.fail()){
		cerr <<"This file is not accessible" << endl;
		exit(1);
	}
	string line;
	text = "";
	while(getline(text_file, line))
		text += line + '\n';
	text_file.close();
}

void refine_text(string &text){
	for(int i=0; i< text.length(); i++)
	    if(isalpha(text[i]) || isspace(text[i]) )
	        text[i] = tolower(text[i]);
	    else
	        text[i] = ' ';
}

void remove_stop_words(string &whole_text){
	ifstream stop_words_file;
	stop_words_file.open("../stop_words.txt");
	string word;
	set<string> stop_words;
	while(getline(stop_words_file, word, ',')){
		if(isBlank(word))
			continue;
		stop_words.insert(word);
	}
	stop_words_file.close();
	string new_text = "";
	stringstream ss(whole_text);
	while(ss >> word){
		if(stop_words.find(word) != stop_words.end())
			continue;
		if(isBlank(word) or word.length() == 1)
			continue;
		new_text += word + ' ';
	}
	whole_text = new_text;	
}

void find_and_sort_frequencies(string whole_text, vector< pair<string, int> > &term_frequencies){
	string word;
	map<string, int> frequency_words;
	stringstream ss(whole_text);
	term_frequencies.clear();
	while(ss >> word){
		if(frequency_words.find(word) == frequency_words.end())
			frequency_words[word] = 1;
		else
			frequency_words[word] = frequency_words[word] + 1;
	}
	for(map<string,int>::iterator it = frequency_words.begin(); it != frequency_words.end(); it++)
		term_frequencies.push_back(make_pair(it->first, it->second));
	sort(term_frequencies.begin(), term_frequencies.end(), sortbysec);
}

void print_frequencies(vector<pair<string, int> > &term_frequencies){
	for(int i=0; i<25; i++)
		cout << term_frequencies[i].first << "  -   " << term_frequencies[i].second << endl;	
}

int main(int argc, char *argv[]){
	if(argc != 2)
	{
		cerr << "Not enough arguments" << endl;
		return 1;
	}
	vector <pair<string, int> > term_frequencies;
	string whole_text = "";
	get_whole_text(argv[1], whole_text);
	refine_text(whole_text);
	remove_stop_words(whole_text);
	find_and_sort_frequencies(whole_text, term_frequencies);
	print_frequencies(term_frequencies);
	return 0;
}
