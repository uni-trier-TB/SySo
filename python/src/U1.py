import os
import re
import time
import argparse
from concurrent.futures import ThreadPoolExecutor


def count_lines(file_path):
    with open(file_path, 'r', encoding='utf-8', errors='ignore') as file:
        line_count = sum(1 for line in file)
    return line_count


def count_lines_in_all_files(folder_path, regex_str, num_threads):

    regex = re.compile(regex_str)

    files_to_process = []

    # for schleife nur für das zusammenstellen aller file_path mit .txt
    for root, dirs, files in os.walk(folder_path):
        for file_name in files:
            if regex.match(file_name):
                file_path = os.path.join(root, file_name)
                files_to_process.append(file_path)

    s_timer = time.time()
    with ThreadPoolExecutor(max_workers=num_threads) as executor:

         # threads die die funktion count_lines ausführen
         resu = executor.map(count_lines, files_to_process)

    e_time = time.time()
    result_time = e_time - s_timer
    print(f'time: {result_time}')
    total_line = sum(resu)
    return total_line


if __name__ == "__main__":

    parser = argparse.ArgumentParser(description='Count lines in all files matching regex in a directory tree.')
    parser.add_argument('folder_path', help='Path to the folder')
    parser.add_argument('regex_str', help='Regular expression string to match files')
    parser.add_argument('num_threads', type=int, help='Number of threads to use')
    
    args = parser.parse_args()

    #neues Argument für die Anzahl an Threads
    total_lines = count_lines_in_all_files(args.folder_path, args.regex_str, args.num_threads)
    print(f'Total number of lines: {total_lines}')

