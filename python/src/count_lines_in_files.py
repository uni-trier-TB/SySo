import os
import re
import argparse
from concurrent.futures import ThreadPoolExecutor, as_completed
import time

def count_lines(file_path):
    """Zählt Zeilen in einer Datei."""
    with open(file_path, 'r', encoding='utf-8', errors='ignore') as file:
        return sum(1 for _ in file)

def get_matching_files(folder_path, regex_str):
    """Sammelt alle Dateien, die zum Regex passen."""
    regex = re.compile(regex_str)
    matching_files = []
    for root, dirs, files in os.walk(folder_path):
        for file_name in files:
            if regex.match(file_name):
                matching_files.append(os.path.join(root, file_name))
    return matching_files

def count_lines_in_all_files_parallel(folder_path, regex_str, num_threads):
    """Zählt Zeilen in allen passenden Dateien mit mehreren Threads."""
    files = get_matching_files(folder_path, regex_str)
    total_lines = 0
    
    start_time = time.time()
    with ThreadPoolExecutor(max_workers=num_threads) as executor:
        futures = {executor.submit(count_lines, file): file for file in files}
        for future in as_completed(futures):
            total_lines += future.result()
    end_time = time.time()

    elapsed = end_time - start_time
    return total_lines, elapsed

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Zähle Zeilen in Dateien (parallel).')
    parser.add_argument('folder_path', help='Pfad zum Ordner')
    parser.add_argument('regex_str', help='Regex zum Filtern der Dateien')
    parser.add_argument('--threads', type=int, default=4, help='Anzahl der Threads (Standard: 4)')
    
    args = parser.parse_args()

    total_lines, elapsed = count_lines_in_all_files_parallel(args.folder_path, args.regex_str, args.threads)
    print(f'Total number of lines: {total_lines}')
    print(f'Elapsed time with {args.threads} threads: {elapsed:.2f} seconds')
