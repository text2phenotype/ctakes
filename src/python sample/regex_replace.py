import re
import os
from typing import List, Dict, Tuple


def parse_regex_file(file_regex: str) -> Dict[str, str]:
    """
    Parse file with regex patterns and create cache
    :param file_regex: Path to the file with RegEx patterns
    :return: Dictionary: (replace_token) => (pattern)
    """
    result = {}
    if os.path.exists(file_regex):

        with open(file_regex) as fp:

            for line in fp:
                if len(line) > 0 and line[0] == "#":
                    pattern_line = fp.readline()
                    result["$" + line[1:].strip('\n')] = pattern_line.strip('\n')
    else:
        print("The file with RegEx patterns isn't exists")

    return result


def regex_replace(text: str, file_regex: str) -> Dict[str, List[Tuple[int, int]]]:
    """
    :param text: Input text
    :param file_regex: Path to the file with RegEx patterns
    :return: Dictionary: (replace_token) => (index[(start, end)])
    """

    result = {}

    regex_map = parse_regex_file(file_regex)
    for token in regex_map:
        regex = regex_map[token]

        indices = []
        for match in re.finditer(regex, text, flags=re.MULTILINE):
            indices.append((match.start(), match.end()))
        result[token] = indices

    return result

# Example:
# res = regex_replace('<Text>', '<path to file with RegEx patterns>')

