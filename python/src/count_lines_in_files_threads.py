import os
import re
import argparse
import threading
import time

#Summe aller Zeilen
sum_lines = 0

def count_lines(file_path):
    global sum_lines
    with open(file_path, 'r', encoding='utf-8', errors='ignore') as file:
        line_count = sum(1 for line in file)
    sum_lines += line_count

def count_lines_in_all_files(folder_path, regex_str):
    threads = []    
    regex = re.compile(regex_str)

    for root, dirs, files in os.walk(folder_path):
        for file_name in files:
            if regex.match(file_name):
                file_path = os.path.join(root, file_name)
                thread = threading.Thread(target=count_lines, args=(file_path, )) 
                threads.append(thread)
                thread.start()

    for thread in threads:
        thread.join()

if __name__ == "__main__":

    parser = argparse.ArgumentParser(description='Count lines in all files matching regex in a directory tree.')
    parser.add_argument('folder_path', help='Path to the folder')
    parser.add_argument('regex_str', help='Regular expression string to match files')    
    args = parser.parse_args()
    
    start_time = time.time()
    count_lines_in_all_files(args.folder_path, args.regex_str)
    end_time = time.time()  

    print(f'Total number of lines: {sum_lines}')
    print(f'Time: {end_time - start_time:.4f} Sekunden')